package com.project.seoulmate.ui.screens.meeting

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.R
import com.project.seoulmate.data.model.CoursePoint
import com.project.seoulmate.data.model.MateInfo
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.model.MeetingDetail
import com.project.seoulmate.data.model.CommentResponse
import com.project.seoulmate.data.repository.CommentRepository
import com.project.seoulmate.data.repository.FavoriteRepository
import com.project.seoulmate.data.repository.MeetingRepository
import com.project.seoulmate.data.repository.ApplicationRepository
import com.project.seoulmate.util.TranslationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MeetingDetailViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle,
    private val meetingRepository: MeetingRepository,
    private val favoriteRepository: FavoriteRepository,
    private val applicationRepository: ApplicationRepository,
    private val commentRepository: CommentRepository,
    private val userActionRepository: com.project.seoulmate.data.repository.UserActionRepository
) : ViewModel() {

    private fun str(resId: Int): String = appContext.getString(resId)

    private val meetingId: String = checkNotNull(savedStateHandle["meetingId"])

    private val _uiState = MutableStateFlow<MeetingDetail?>(null)
    val uiState: StateFlow<MeetingDetail?> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userActionEvent = MutableSharedFlow<UserActionResult>()
    val userActionEvent: SharedFlow<UserActionResult> = _userActionEvent.asSharedFlow()

    // --- 댓글 상태 ---
    private val _comments = MutableStateFlow<List<CommentResponse>>(emptyList())
    val comments: StateFlow<List<CommentResponse>> = _comments.asStateFlow()

    private val _commentsLoading = MutableStateFlow(false)
    val commentsLoading: StateFlow<Boolean> = _commentsLoading.asStateFlow()

    // commentId → 영어 번역 결과 (캐시)
    private val _translatedById = MutableStateFlow<Map<Long, String>>(emptyMap())
    val translatedById: StateFlow<Map<Long, String>> = _translatedById.asStateFlow()

    // 번역 진행 중인 commentId 집합
    private val _translatingIds = MutableStateFlow<Set<Long>>(emptySet())
    val translatingIds: StateFlow<Set<Long>> = _translatingIds.asStateFlow()

    // --- 만남 본문 번역 (제목/소개) ---
    private val _translatedTitle = MutableStateFlow<String?>(null)
    val translatedTitle: StateFlow<String?> = _translatedTitle.asStateFlow()

    private val _translatedDescription = MutableStateFlow<String?>(null)
    val translatedDescription: StateFlow<String?> = _translatedDescription.asStateFlow()

    private val _isTranslatingTitle = MutableStateFlow(false)
    val isTranslatingTitle: StateFlow<Boolean> = _isTranslatingTitle.asStateFlow()

    private val _isTranslatingDescription = MutableStateFlow(false)
    val isTranslatingDescription: StateFlow<Boolean> = _isTranslatingDescription.asStateFlow()

    /** 디바이스가 한국어가 아니면 번역 UI 노출 */
    val translationEnabled: Boolean get() = TranslationService.isEnabled
    val translationLabel: String get() = TranslationService.targetLabel()

    sealed class UserActionResult {
        data class Success(val message: String) : UserActionResult()
        data class Error(val message: String) : UserActionResult()
    }

    init {
        loadMeetingDetail()
        loadComments()
        // 앱 첫 사용 시 한국어→영어 번역 모델 선다운로드 (Wi-Fi 필요)
        viewModelScope.launch {
            TranslationService.ensureModelDownloaded().onFailure {
                Timber.w(it, "Translation model preload failed (will retry on click)")
            }
        }
    }

    private fun loadMeetingDetail() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 실제 API 호출로 변경
                val result = meetingRepository.getMeetingDetail(meetingId)
                result.onSuccess { detail ->
                    _uiState.value = detail
                    // 서버 응답에서 찜 상태 가져오기 (MeetingDetailResponse.isFavorite 활용 필요)
                    // TODO: MeetingDetail에 isFavorite 필드 추가 필요
                    // 임시로 찜 목록에서 확인하는 방식 사용
                    checkIsFavorited()
                }.onFailure { error ->
                    Timber.e(error, "Failed to load meeting detail")
                    // 에러 시 더미 데이터 사용 (임시)
                    loadDummyData()
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception loading meeting detail")
                loadDummyData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 현재 만남이 찜 목록에 있는지 확인
     */
    private fun checkIsFavorited() {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token ?: return@launch

                // 찜 목록 조회
                // TODO: 백엔드 API 개선 필요 - MeetingDetailResponse에 isFavorite 필드 추가하거나
                //       GET /api/favorites/exists?targetId=X 엔드포인트 추가 권장
                val result = favoriteRepository.getFavorites(idToken, targetType = "MEETUP", page = 0, size = 100)
                result.onSuccess { pageResponse ->
                    // 현재 만남 ID가 찜 목록에 있는지 확인
                    val isFavorited = pageResponse.content.any {
                        it.targetId.toString() == meetingId
                    }
                    _isFavorite.value = isFavorited
                }.onFailure { error ->
                    Timber.e(error, "Failed to check favorite status")
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception checking favorite status")
            }
        }
    }

    /**
     * 찜 토글 (추가/취소)
     */
    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token

                if (idToken == null) {
                    Timber.e("Firebase token is null")
                    return@launch
                }

                if (_isFavorite.value) {
                    // 찜 취소
                    val result = favoriteRepository.removeFavorite(
                        token = idToken,
                        targetType = "MEETUP",
                        targetId = meetingId.toLongOrNull() ?: 0L
                    )
                    result.onSuccess {
                        _isFavorite.value = false
                        Timber.d("Favorite removed successfully")
                    }.onFailure { error ->
                        Timber.e(error, "Failed to remove favorite")
                    }
                } else {
                    // 찜 추가
                    val result = favoriteRepository.addFavorite(
                        token = idToken,
                        targetType = "MEETUP",
                        targetId = meetingId.toLongOrNull() ?: 0L
                    )
                    result.onSuccess {
                        _isFavorite.value = true
                        Timber.d("Favorite added successfully")
                    }.onFailure { error ->
                        Timber.e(error, "Failed to add favorite")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during favorite toggle")
            }
        }
    }

    /**
     * 더미 데이터 로드 (임시)
     */
    private fun loadDummyData() {
        val dummyMeeting = Meeting(
            id = meetingId,
            title = "창덕궁 탐방 및 맛집 방문",
            time = "3월 23일 오후 6-7시",
            price = "약 20,000원",
            rating = "5.0",
            imageRes = R.drawable.img_recommend_1,
            tags = listOf("#관광", "#맛집", "#커뮤니티")
        )

        val detail = MeetingDetail(
            meeting = dummyMeeting,
            location = "종로구",
            timeElapsed = "10시간 전",
            dateAndTime = "3월 23일 오후 6-7시",
            description = "창덕궁의 숨겨진 명소를 관람한 후 북촌 한옥 마을로 한식 맛집을 함께 가요^^\n\n저녁 비용과 방문 비용 포함해서 전부 2만원만 소요!",
            courses = listOf(
                CoursePoint("창덕궁 (만남 시작)", isStart = true, lat = 37.5824, lng = 126.9917),
                CoursePoint("창경궁 대온실", lat = 37.5816, lng = 126.9961),
                CoursePoint("종묘", lat = 37.5746, lng = 126.9940),
                CoursePoint("북촌 조향사의 집", lat = 37.5815, lng = 126.9840),
                CoursePoint("익선동한옥거리 (만남 종료)", isEnd = true, lat = 37.5745, lng = 126.9890)
            ),
            mateInfo = MateInfo(
                name = "소율이",
                profileRes = R.drawable.img_recommend_1,
                rating = "4.22",
                reviewCount = 83,
                bio = "인스타 @insoul 유튜버\n관광학부 전공으로 재직... [더보기]",
                isVerified = true
            ),
            mateOtherMeetings = listOf(
                Meeting(
                    id = "meeting_other_1",
                    title = "창덕궁 탐방 및 맛집",
                    time = "23일 오후 7~9시",
                    price = "예상 ₩20,000",
                    rating = "5.0",
                    imageRes = R.drawable.img_recommend_1,
                    tags = listOf("관광", "맛집", "커뮤니티")
                ),
                Meeting(
                    id = "meeting_other_2",
                    title = "창덕궁 탐방 및 맛집",
                    time = "28일 오후 7~9시",
                    price = "예상 ₩20,000",
                    rating = "5.0",
                    imageRes = R.drawable.img_recommend_2,
                    tags = listOf("여유", "맛집", "커뮤니티")
                )
            ),
            isHost = false // 더미 데이터는 호스트 아님
        )

        _uiState.value = detail
    }

    /**
     * 신고 (게시글 또는 사용자)
     * reason이 "POST_CONTENT"면 게시글 신고, "USER_BEHAVIOR"면 사용자 신고
     */
    fun reportUser(reason: String, description: String) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val token = user?.getIdToken(false)?.await()?.token

                if (token == null) {
                    _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_login_required)))
                    return@launch
                }

                val currentDetail = _uiState.value
                if (currentDetail == null) {
                    _userActionEvent.emit(UserActionResult.Error("만남 정보를 불러올 수 없습니다"))
                    return@launch
                }

                // reason에 따라 targetType 결정
                val (targetType, targetId) = when (reason) {
                    "POST_CONTENT" -> "MEETUP" to meetingId.toLongOrNull()
                    "USER_BEHAVIOR" -> "USER" to currentDetail.mateInfo.id
                    else -> "MEETUP" to meetingId.toLongOrNull()
                }

                if (targetId == null) {
                    _userActionEvent.emit(UserActionResult.Error("잘못된 요청입니다"))
                    return@launch
                }

                // 백엔드 API 스펙에 맞게 reason 매핑
                val apiReason = when (reason) {
                    "POST_CONTENT" -> "INAPPROPRIATE"
                    "USER_BEHAVIOR" -> "OTHER"
                    else -> "OTHER"
                }

                val request = com.project.seoulmate.data.model.ReportRequest(
                    targetType = targetType,
                    targetId = targetId,
                    reason = apiReason,
                    description = description
                )

                val result = userActionRepository.report(token, request)
                result.onSuccess {
                    _userActionEvent.emit(UserActionResult.Success(str(R.string.toast_report_received)))
                }.onFailure { error ->
                    Timber.e(error, "Failed to report")
                    _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_report_error)))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to report")
                _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_report_error)))
            }
        }
    }

    /**
     * 사용자 차단
     */
    fun blockUser() {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val token = user?.getIdToken(false)?.await()?.token

                if (token == null) {
                    _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_login_required)))
                    return@launch
                }

                val currentDetail = _uiState.value
                if (currentDetail == null) {
                    _userActionEvent.emit(UserActionResult.Error("만남 정보를 불러올 수 없습니다"))
                    return@launch
                }

                val request = com.project.seoulmate.data.model.BlockRequest(
                    blockedUserId = currentDetail.mateInfo.id
                )

                val result = userActionRepository.block(token, request)
                result.onSuccess {
                    _userActionEvent.emit(UserActionResult.Success(str(R.string.toast_user_blocked)))
                }.onFailure { error ->
                    Timber.e(error, "Failed to block user")
                    _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_block_error)))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to block user")
                _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_block_error)))
            }
        }
    }

    /**
     * 만남 신청 (예약 요청)
     * @param message 신청 메시지
     */
    fun applyForMeeting(message: String) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token

                if (idToken == null) {
                    _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_login_required)))
                    return@launch
                }

                val meetingIdLong = meetingId.toLongOrNull()
                if (meetingIdLong == null) {
                    _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_invalid_meeting_id)))
                    return@launch
                }

                _isLoading.value = true
                Timber.d("Applying for meeting: meetingId=$meetingIdLong, message=$message")
                val result = applicationRepository.createApplication(idToken, meetingIdLong, message)

                result.onSuccess { applicationResponse ->
                    Timber.d("Application created: id=${applicationResponse.id}, status=${applicationResponse.status}")
                    _userActionEvent.emit(UserActionResult.Success(str(R.string.toast_apply_success)))
                    // 상세 화면 새로고침 (신청 상태 반영)
                    loadMeetingDetail()
                }.onFailure { error ->
                    Timber.e(error, "Failed to create application")
                    _userActionEvent.emit(UserActionResult.Error(error.message ?: str(R.string.toast_apply_failed)))
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during apply for meeting")
                _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_apply_generic_error)))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 만남 삭제 (호스트만 가능)
     */
    fun deleteMeeting(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token

                if (idToken == null) {
                    _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_login_required)))
                    return@launch
                }

                _isLoading.value = true
                val result = meetingRepository.deleteMeeting(idToken, meetingId)

                result.onSuccess {
                    Timber.d("Meeting deleted successfully")
                    _userActionEvent.emit(UserActionResult.Success(str(R.string.toast_meeting_deleted)))
                    onSuccess()
                }.onFailure { error ->
                    Timber.e(error, "Failed to delete meeting")
                    _userActionEvent.emit(UserActionResult.Error(error.message ?: str(R.string.toast_meeting_delete_failed)))
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during delete meeting")
                _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_meeting_delete_generic_error)))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 만남 상태 변경 (호스트만 가능)
     * 상태 변경 시 백엔드에서 참여자 전체에게 알림을 자동으로 발송합니다.
     *
     * @param status 변경할 상태 (OPEN, CLOSED, COMPLETED 등)
     */
    fun updateMeetingStatus(status: String) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token

                if (idToken == null) {
                    _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_login_required)))
                    return@launch
                }

                _isLoading.value = true
                Timber.d("Updating meeting status to: $status")

                val result = meetingRepository.updateMeetingStatus(idToken, meetingId, status)

                result.onSuccess {
                    Timber.d("Meeting status updated successfully to $status")

                    val message = when (status) {
                        "CLOSED" -> str(R.string.toast_status_closed_message)
                        "COMPLETED" -> str(R.string.toast_status_completed_message)
                        "OPEN" -> str(R.string.toast_status_open_message)
                        else -> str(R.string.toast_status_changed_generic)
                    }

                    _userActionEvent.emit(UserActionResult.Success(message))

                    // 상세 정보 새로고침
                    loadMeetingDetail()
                }.onFailure { error ->
                    Timber.e(error, "Failed to update meeting status")
                    _userActionEvent.emit(UserActionResult.Error(error.message ?: str(R.string.toast_status_change_failed)))
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during update meeting status")
                _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_status_change_generic_error)))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 만남 마감 (호스트만 가능)
     * 백엔드에서 참여자 전체에게 "만남이 마감되었습니다" 알림을 발송합니다.
     */
    fun closeMeeting() {
        updateMeetingStatus("CLOSED")
    }

    /**
     * 만남 완료 처리 (호스트만 가능)
     * 백엔드에서 참여자 전체에게 "만남이 완료되었습니다" 알림을 발송합니다.
     */
    fun completeMeeting() {
        updateMeetingStatus("COMPLETED")
    }

    /**
     * 만남 재개 (호스트만 가능)
     * 마감된 만남을 다시 열 때 사용합니다.
     */
    fun reopenMeeting() {
        updateMeetingStatus("OPEN")
    }

    // --------------------------- 댓글 ---------------------------

    /** 현재 Firebase 사용자의 idToken (없으면 null) */
    private suspend fun currentToken(): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return runCatching { user.getIdToken(false).await()?.token }.getOrNull()
    }

    fun loadComments() {
        viewModelScope.launch {
            val id = meetingId.toLongOrNull() ?: return@launch
            _commentsLoading.value = true
            try {
                val token = currentToken()
                commentRepository.getComments(token, id, page = 0, size = 50)
                    .onSuccess { page -> _comments.value = page.content }
                    .onFailure { Timber.e(it, "Failed to load comments") }
            } finally {
                _commentsLoading.value = false
            }
        }
    }

    fun submitComment(content: String, isPrivate: Boolean) {
        viewModelScope.launch {
            val token = currentToken()
            if (token == null) {
                _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_login_required)))
                return@launch
            }
            val id = meetingId.toLongOrNull() ?: return@launch
            commentRepository.createComment(token, id, content, isPrivate)
                .onSuccess { created ->
                    _comments.update { it + created }
                }
                .onFailure { e ->
                    Timber.e(e, "createComment failed")
                    _userActionEvent.emit(
                        UserActionResult.Error(e.message ?: str(R.string.toast_comment_create_failed))
                    )
                }
        }
    }

    fun editComment(commentId: Long, newContent: String) {
        viewModelScope.launch {
            val token = currentToken() ?: run {
                _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_login_required)))
                return@launch
            }
            val id = meetingId.toLongOrNull() ?: return@launch
            commentRepository.updateComment(token, id, commentId, newContent)
                .onSuccess { updated ->
                    _comments.update { list ->
                        list.map { if (it.id == commentId) updated else it }
                    }
                    // 수정되면 기존 번역 캐시 제거
                    _translatedById.update { it - commentId }
                }
                .onFailure { e ->
                    Timber.e(e, "updateComment failed")
                    _userActionEvent.emit(
                        UserActionResult.Error(e.message ?: str(R.string.toast_comment_update_failed))
                    )
                }
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            val token = currentToken() ?: run {
                _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_login_required)))
                return@launch
            }
            val id = meetingId.toLongOrNull() ?: return@launch
            commentRepository.deleteComment(token, id, commentId)
                .onSuccess {
                    _comments.update { list -> list.filterNot { it.id == commentId } }
                    _translatedById.update { it - commentId }
                }
                .onFailure { e ->
                    Timber.e(e, "deleteComment failed")
                    _userActionEvent.emit(
                        UserActionResult.Error(e.message ?: str(R.string.toast_comment_delete_failed))
                    )
                }
        }
    }

    /**
     * 댓글 번역 아이콘 클릭. 이미 번역된 상태면 토글로 원문 복귀.
     */
    fun toggleTranslate(commentId: Long, text: String) {
        if (_translatedById.value.containsKey(commentId)) {
            _translatedById.update { it - commentId }
            return
        }
        if (commentId in _translatingIds.value) return

        viewModelScope.launch {
            _translatingIds.update { it + commentId }
            TranslationService.translate(text)
                .onSuccess { translated ->
                    _translatedById.update { it + (commentId to translated) }
                }
                .onFailure { e ->
                    Timber.e(e, "translate failed")
                    _userActionEvent.emit(
                        UserActionResult.Error(str(R.string.toast_translate_failed))
                    )
                }
            _translatingIds.update { it - commentId }
        }
    }

    /** 만남 제목 번역 토글. */
    fun toggleTranslateTitle() {
        if (_translatedTitle.value != null) {
            _translatedTitle.value = null
            return
        }
        val text = _uiState.value?.meeting?.title ?: return
        if (_isTranslatingTitle.value) return
        viewModelScope.launch {
            _isTranslatingTitle.value = true
            TranslationService.translate(text)
                .onSuccess { _translatedTitle.value = it }
                .onFailure { e ->
                    Timber.e(e, "translate title failed")
                    _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_translate_failed)))
                }
            _isTranslatingTitle.value = false
        }
    }

    /** 만남 소개 번역 토글. */
    fun toggleTranslateDescription() {
        if (_translatedDescription.value != null) {
            _translatedDescription.value = null
            return
        }
        val text = _uiState.value?.description ?: return
        if (_isTranslatingDescription.value) return
        viewModelScope.launch {
            _isTranslatingDescription.value = true
            TranslationService.translate(text)
                .onSuccess { _translatedDescription.value = it }
                .onFailure { e ->
                    Timber.e(e, "translate description failed")
                    _userActionEvent.emit(UserActionResult.Error(str(R.string.toast_translate_failed)))
                }
            _isTranslatingDescription.value = false
        }
    }
}