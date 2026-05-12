package com.project.seoulmate.ui.screens.notification

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.R
import com.project.seoulmate.data.repository.ApplicationRepository
import com.project.seoulmate.data.repository.NotificationRepository
import com.project.seoulmate.data.repository.MeetingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val notificationRepository: NotificationRepository,
    private val meetingRepository: MeetingRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<UnifiedNotification>>(emptyList())
    val notifications: StateFlow<List<UnifiedNotification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _actionResult = MutableStateFlow<ActionResult?>(null)
    val actionResult: StateFlow<ActionResult?> = _actionResult.asStateFlow()

    /**
     * UI에 전달할 액션 결과. messageRes(필수)로 stringResource 변환하며,
     * 백엔드가 내려준 동적 메시지가 있으면 dynamicMessage에 담아 fallback으로 표시.
     */
    sealed class ActionResult {
        data class Success(@StringRes val messageRes: Int) : ActionResult()
        data class Error(
            @StringRes val messageRes: Int,
            val dynamicMessage: String? = null
        ) : ActionResult()
    }

    init {
        loadAllNotifications()
    }

    /**
     * 모든 알림 조회 (Application + Notification 통합)
     * - 호스트가 받은 메이트 신청 (Application)
     * - 신청자가 받은 승인/거절 알림 (Notification)
     */
    fun loadAllNotifications() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token

                if (idToken == null) {
                    Timber.e("NotificationVM - Firebase token is null")
                    _notifications.value = emptyList()
                    return@launch
                }

                // 1. 내가 받은 상태 알림 (승인/거절) 조회
                val notificationResult = notificationRepository.getNotifications(
                    token = idToken,
                    page = 0,
                    size = 50
                )

                // 2. 호스트로서 받은 메이트 신청 조회
                // TODO: 실제로는 내가 호스트인 모든 만남의 신청을 가져와야 함
                // 현재는 백엔드 API 한계로 빈 목록 사용
                // 백엔드에 GET /api/applications/received 같은 엔드포인트가 필요
                val myApplicationsResult = applicationRepository.getMyApplications(idToken)

                val unifiedList = mutableListOf<UnifiedNotification>()

                // 알림 데이터 추가 (승인/거절 알림)
                notificationResult.onSuccess { pageResponse ->
                    pageResponse.content.forEach { notification ->
                        unifiedList.add(
                            UnifiedNotification.StatusNotification(
                                notification = notification,
                                sortTimestamp = parseIsoTimestamp(notification.createdAt)
                            )
                        )
                    }
                }.onFailure { error ->
                    Timber.e(error, "NotificationVM - Failed to load notifications")
                }

                // 신청 데이터 추가 (호스트가 받은 신청)
                // TODO: 현재는 "내가 보낸 신청"을 가져오는 API만 있음
                // 백엔드에서 "내가 받은 신청" API 추가 필요
                myApplicationsResult.onSuccess { applications ->
                    applications.forEach { application ->
                        // 임시: 내가 보낸 신청도 알림 목록에 포함 (상태 확인용)
                        // 실제로는 호스트가 받은 신청만 표시해야 함
                        unifiedList.add(
                            UnifiedNotification.ApplicationNotification(
                                application = application,
                                sortTimestamp = parseIsoTimestamp(application.createdAt)
                            )
                        )
                    }
                }.onFailure { error ->
                    Timber.e(error, "NotificationVM - Failed to load applications")
                }

                // 시간순 정렬 (최신순)
                _notifications.value = unifiedList.sortedByDescending { it.sortTimestamp }

                Timber.d("NotificationVM - Total notifications loaded: ${_notifications.value.size}")
            } catch (e: Exception) {
                Timber.e(e, "NotificationVM - Failed to load all notifications")
                _notifications.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 신청 승인
     * 승인 후 정원 도달 시 자동으로 만남을 마감 처리합니다.
     */
    fun approveApplication(applicationId: Long) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token

                if (idToken == null) {
                    _actionResult.value = ActionResult.Error(R.string.toast_login_required)
                    return@launch
                }

                _isLoading.value = true
                Timber.d("NotificationVM - Approving application: $applicationId")

                val result = applicationRepository.approveApplication(idToken, applicationId)

                result.onSuccess { response ->
                    Timber.d("NotificationVM - Application approved: ${response.id}, status=${response.status}")

                    // 백엔드에서 정원 체크 및 자동 마감을 처리하므로 클라이언트 체크는 불필요
                    // checkAndCloseMeetingIfFull(idToken, response.meetupId)

                    _actionResult.value = ActionResult.Success(R.string.toast_apply_approved)
                    // 목록 새로고침
                    loadAllNotifications()
                }.onFailure { error ->
                    Timber.e(error, "NotificationVM - Failed to approve application")
                    _actionResult.value = ActionResult.Error(
                        messageRes = R.string.toast_apply_approve_failed,
                        dynamicMessage = error.message
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "NotificationVM - Exception during approve")
                _actionResult.value = ActionResult.Error(R.string.toast_approve_generic_error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 만남 정원 체크 및 자동 마감 처리
     * 현재 인원이 최대 정원에 도달하면 만남 상태를 CLOSED로 변경합니다.
     * 상태 변경 시 백엔드에서 참여자 전체에게 알림을 발송합니다.
     */
    private suspend fun checkAndCloseMeetingIfFull(token: String, meetupId: Long) {
        try {
            Timber.d("NotificationVM - Checking capacity for meetup: $meetupId")

            // 만남 상세 정보 조회
            val meetingResult = meetingRepository.getMeetingDetail(meetupId.toString())

            meetingResult.onSuccess { meetingDetail ->
                val maxMembers = meetingDetail.maxMembers
                val currentMembers = meetingDetail.currentMembers
                val status = meetingDetail.status

                Timber.d("NotificationVM - Meeting capacity: $currentMembers/$maxMembers, status=$status")

                // 정원이 설정되어 있고, 현재 인원이 최대 정원에 도달했으며, 상태가 OPEN인 경우
                if (maxMembers != null && currentMembers != null &&
                    currentMembers >= maxMembers && status == "OPEN") {

                    Timber.d("NotificationVM - Meeting is full! Auto-closing meetup $meetupId")

                    // 만남 상태를 CLOSED로 변경
                    // 백엔드에서 상태 변경 시 참여자들에게 "만남이 마감되었습니다" 알림을 자동 발송
                    updateMeetingStatus(token, meetupId, "CLOSED")

                    _actionResult.value = ActionResult.Success(
                        R.string.toast_apply_approved_meeting_full
                    )
                }
            }.onFailure { error ->
                Timber.e(error, "NotificationVM - Failed to load meeting detail for capacity check")
            }
        } catch (e: Exception) {
            Timber.e(e, "NotificationVM - Exception during capacity check")
        }
    }

    /**
     * 만남 상태를 변경합니다.
     * 백엔드에서 상태 변경 시 참여자 전체에게 알림을 자동으로 발송합니다.
     *
     * @param meetupId 만남 ID
     * @param status 변경할 상태 (CLOSED, COMPLETED 등)
     */
    private suspend fun updateMeetingStatus(token: String, meetupId: Long, status: String) {
        try {
            Timber.d("NotificationVM - Updating meeting status: meetupId=$meetupId, status=$status")

            val result = meetingRepository.updateMeetingStatus(token, meetupId.toString(), status)

            result.onSuccess {
                Timber.d("NotificationVM - Meeting status updated successfully")
                // 백엔드에서 상태 변경 시 참여자들에게 알림을 자동 발송합니다
                when (status) {
                    "CLOSED" -> Timber.d("NotificationVM - Meeting closed, participants will be notified by backend")
                    "COMPLETED" -> Timber.d("NotificationVM - Meeting completed, participants will be notified by backend")
                }
            }.onFailure { error ->
                Timber.e(error, "NotificationVM - Failed to update meeting status")
            }
        } catch (e: Exception) {
            Timber.e(e, "NotificationVM - Exception during status update")
        }
    }

    /**
     * 신청 거절
     */
    fun rejectApplication(applicationId: Long) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token

                if (idToken == null) {
                    _actionResult.value = ActionResult.Error(R.string.toast_login_required)
                    return@launch
                }

                _isLoading.value = true
                Timber.d("NotificationVM - Rejecting application: $applicationId")

                val result = applicationRepository.rejectApplication(idToken, applicationId)

                result.onSuccess { response ->
                    Timber.d("NotificationVM - Application rejected: ${response.id}, status=${response.status}")
                    _actionResult.value = ActionResult.Success(R.string.toast_apply_rejected)
                    // 목록 새로고침
                    loadAllNotifications()
                }.onFailure { error ->
                    Timber.e(error, "NotificationVM - Failed to reject application")
                    _actionResult.value = ActionResult.Error(
                        messageRes = R.string.toast_apply_reject_failed,
                        dynamicMessage = error.message
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "NotificationVM - Exception during reject")
                _actionResult.value = ActionResult.Error(R.string.toast_reject_generic_error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 알림 읽음 처리
     */
    fun markNotificationAsRead(notificationId: Long) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token ?: return@launch

                notificationRepository.markAsRead(idToken, notificationId)
                    .onSuccess {
                        Timber.d("NotificationVM - Notification marked as read: $notificationId")
                        // UI 업데이트 (필요시)
                    }
                    .onFailure { error ->
                        Timber.e(error, "NotificationVM - Failed to mark as read")
                    }
            } catch (e: Exception) {
                Timber.e(e, "NotificationVM - Exception during mark as read")
            }
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}