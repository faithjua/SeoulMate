package com.project.seoulmate.ui.screens.meeting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.R
import com.project.seoulmate.data.model.CoursePoint
import com.project.seoulmate.data.model.MateInfo
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.model.MeetingDetail
import com.project.seoulmate.data.repository.FavoriteRepository
import com.project.seoulmate.data.repository.MeetingRepository
import com.project.seoulmate.data.repository.ApplicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MeetingDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val meetingRepository: MeetingRepository,
    private val favoriteRepository: FavoriteRepository,
    private val applicationRepository: ApplicationRepository
) : ViewModel() {

    private val meetingId: String = checkNotNull(savedStateHandle["meetingId"])

    private val _uiState = MutableStateFlow<MeetingDetail?>(null)
    val uiState: StateFlow<MeetingDetail?> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userActionEvent = MutableSharedFlow<UserActionResult>()
    val userActionEvent: SharedFlow<UserActionResult> = _userActionEvent.asSharedFlow()

    sealed class UserActionResult {
        data class Success(val message: String) : UserActionResult()
        data class Error(val message: String) : UserActionResult()
    }

    init {
        loadMeetingDetail()
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
            )
        )

        _uiState.value = detail
    }

    /**
     * 사용자 신고
     */
    fun reportUser(reason: String, description: String) {
        viewModelScope.launch {
            try {
                // TODO: 실제 API 호출 구현 필요
                // val result = reportRepository.reportUser(userId, reason, description)
                Timber.d("Report user - reason: $reason, description: $description")
                _userActionEvent.emit(UserActionResult.Success("신고가 접수되었습니다"))
            } catch (e: Exception) {
                Timber.e(e, "Failed to report user")
                _userActionEvent.emit(UserActionResult.Error("신고 처리 중 오류가 발생했습니다"))
            }
        }
    }

    /**
     * 사용자 차단
     */
    fun blockUser() {
        viewModelScope.launch {
            try {
                // TODO: 실제 API 호출 구현 필요
                // val result = blockRepository.blockUser(userId)
                Timber.d("Block user")
                _userActionEvent.emit(UserActionResult.Success("사용자를 차단했습니다"))
            } catch (e: Exception) {
                Timber.e(e, "Failed to block user")
                _userActionEvent.emit(UserActionResult.Error("차단 처리 중 오류가 발생했습니다"))
            }
        }
    }

    /**
     * 만남 신청 (예약 요청)
     */
    fun applyForMeeting() {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token

                if (idToken == null) {
                    _userActionEvent.emit(UserActionResult.Error("로그인이 필요합니다"))
                    return@launch
                }

                val meetingIdLong = meetingId.toLongOrNull()
                if (meetingIdLong == null) {
                    _userActionEvent.emit(UserActionResult.Error("잘못된 만남 ID입니다"))
                    return@launch
                }

                _isLoading.value = true
                val result = applicationRepository.createApplication(idToken, meetingIdLong)

                result.onSuccess { applicationResponse ->
                    Timber.d("Application created: ${applicationResponse.id}")
                    _userActionEvent.emit(UserActionResult.Success("예약 요청이 완료되었습니다"))
                    // 상세 화면 새로고침 (신청 상태 반영)
                    loadMeetingDetail()
                }.onFailure { error ->
                    Timber.e(error, "Failed to create application")
                    _userActionEvent.emit(UserActionResult.Error(error.message ?: "예약 요청 실패"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during apply for meeting")
                _userActionEvent.emit(UserActionResult.Error("예약 요청 중 오류가 발생했습니다"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
