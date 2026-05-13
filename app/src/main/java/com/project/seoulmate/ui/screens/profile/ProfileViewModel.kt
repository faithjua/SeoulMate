package com.project.seoulmate.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.model.ReviewItem
import com.project.seoulmate.data.remote.BadgeResponse
import com.project.seoulmate.data.repository.MeetingRepository
import com.project.seoulmate.data.repository.ImageRepository
import com.project.seoulmate.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val meetingRepository: MeetingRepository,
    private val imageRepository: ImageRepository,
    private val userRepository: UserRepository,
    private val userPreferences: com.project.seoulmate.data.local.UserPreferences
) : ViewModel() {

    private val _allMeetings = MutableStateFlow<List<Meeting>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _totalMeetings = MutableStateFlow(0)
    val totalMeetings: StateFlow<Int> = _totalMeetings.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> = _profileImageUrl.asStateFlow()

    private val _uploadingImage = MutableStateFlow(false)
    val uploadingImage: StateFlow<Boolean> = _uploadingImage.asStateFlow()

    private val _badges = MutableStateFlow<List<BadgeResponse>>(emptyList())
    val badges: StateFlow<List<BadgeResponse>> = _badges.asStateFlow()

    private val _loadingBadges = MutableStateFlow(false)
    val loadingBadges: StateFlow<Boolean> = _loadingBadges.asStateFlow()

    private val _reviews = MutableStateFlow<List<ReviewItem>>(emptyList())
    val reviews: StateFlow<List<ReviewItem>> = _reviews.asStateFlow()

    private val _loadingReviews = MutableStateFlow(false)
    val loadingReviews: StateFlow<Boolean> = _loadingReviews.asStateFlow()

    private val _totalReviews = MutableStateFlow(0L)
    val totalReviews: StateFlow<Long> = _totalReviews.asStateFlow()

    private val _bio = MutableStateFlow<String>("")
    val bio: StateFlow<String> = _bio.asStateFlow()

    private val _updatingBio = MutableStateFlow(false)
    val updatingBio: StateFlow<Boolean> = _updatingBio.asStateFlow()

    val myMeetings: StateFlow<List<Meeting>> = combine(_allMeetings, _searchQuery) { meetings, query ->
        if (query.isBlank()) {
            meetings
        } else {
            meetings.filter { it.title.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadMyMeetings()
        loadMyBadges()
        loadMyReviews()
    }

    /**
     * 내가 호스팅한 만남 목록 로드
     */
    fun loadMyMeetings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = FirebaseAuth.getInstance().currentUser
                Timber.d("ProfileViewModel - Current user: ${user?.uid}")

                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token
                Timber.d("ProfileViewModel - Token obtained: ${idToken != null}")

                // UserPreferences에서 저장된 실제 회원 ID 가져오기
                val currentUserId = userPreferences.getMemberId()
                Timber.d("ProfileViewModel - Retrieved memberId from preferences: $currentUserId")

                if (idToken != null && currentUserId != null) {
                    Timber.d("ProfileViewModel - Calling getUserMeetups API with memberId: $currentUserId")
                    val result = meetingRepository.getUserMeetups(
                        token = idToken,
                        memberId = currentUserId,
                        page = 0,
                        size = 100 // 모든 만남 가져오기
                    )

                    result.onSuccess { pageResponse ->
                        Timber.d("ProfileViewModel - Success! Total elements: ${pageResponse.totalElements}, Content size: ${pageResponse.content.size}")
                        _allMeetings.value = pageResponse.content
                        _totalMeetings.value = pageResponse.totalElements.toInt()
                        Timber.d("ProfileViewModel - Meetings loaded: ${pageResponse.content.map { it.id }}")
                    }.onFailure { error ->
                        Timber.e(error, "ProfileViewModel - API call failed: ${error.message}")
                        _allMeetings.value = emptyList()
                        _totalMeetings.value = 0
                    }
                } else {
                    if (idToken == null) {
                        Timber.e("ProfileViewModel - Firebase token is null")
                    }
                    if (currentUserId == null) {
                        Timber.e("ProfileViewModel - Member ID not found. User may need to re-login.")
                    }
                    _allMeetings.value = emptyList()
                    _totalMeetings.value = 0
                }
            } catch (e: Exception) {
                Timber.e(e, "ProfileViewModel - Exception: ${e.message}")
                _allMeetings.value = emptyList()
                _totalMeetings.value = 0
            } finally {
                _isLoading.value = false
                Timber.d("ProfileViewModel - Loading finished")
            }
        }
    }

    /**
     * 검색어 업데이트
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * 새로고침
     */
    fun refresh() {
        loadMyMeetings()
    }

    /**
     * 만남 삭제
     */
    fun deleteMeeting(meetingId: String) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token

                if (idToken == null) {
                    Timber.e("ProfileViewModel - Firebase token is null")
                    return@launch
                }

                _isLoading.value = true
                val result = meetingRepository.deleteMeeting(idToken, meetingId)

                result.onSuccess {
                    Timber.d("ProfileViewModel - Meeting deleted successfully: $meetingId")
                    // 삭제 후 목록 새로고침
                    loadMyMeetings()
                }.onFailure { error ->
                    Timber.e(error, "ProfileViewModel - Failed to delete meeting: ${error.message}")
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Timber.e(e, "ProfileViewModel - Exception during delete meeting")
                _isLoading.value = false
            }
        }
    }

    /**
     * 프로필 이미지 업로드
     * @param uri 선택한 이미지의 URI
     */
    fun uploadProfileImage(uri: Uri) {
        viewModelScope.launch {
            _uploadingImage.value = true
            try {
                Timber.d("ProfileViewModel - Starting profile image upload")

                // 1. 이미지를 S3에 업로드
                val uploadResult = imageRepository.uploadImage(uri, "profiles")

                uploadResult.onSuccess { imageUrl ->
                    Timber.d("ProfileViewModel - Image uploaded to S3: $imageUrl")

                    // 2. 로컬 캐시에 저장 (앱 재시작 시에도 유지)
                    userPreferences.saveProfileImageUrl(imageUrl)
                    Timber.d("ProfileViewModel - Profile image URL saved to local cache")

                    // 3. UI에 바로 반영
                    _profileImageUrl.value = imageUrl

                    // 4. 백엔드에 URL 저장 시도 (실패해도 로컬 상태는 유지)
                    val updateResult = userRepository.updateProfileImage(imageUrl)

                    updateResult.onSuccess {
                        Timber.d("ProfileViewModel - Profile image updated in backend successfully")
                    }.onFailure { error ->
                        Timber.e(error, "ProfileViewModel - Failed to update profile image in backend: ${error.message}")
                        // 백엔드 저장 실패해도 로컬 캐시는 유지됨
                    }
                }.onFailure { error ->
                    Timber.e(error, "ProfileViewModel - Failed to upload image: ${error.message}")
                }
            } catch (e: Exception) {
                Timber.e(e, "ProfileViewModel - Exception during profile image upload")
            } finally {
                _uploadingImage.value = false
            }
        }
    }

    /**
     * 내 프로필 정보 로드
     */
    fun loadProfile() {
        viewModelScope.launch {
            try {
                // 1. 로컬 캐시에서 먼저 로드 (즉시 UI 반영)
                val cachedImageUrl = userPreferences.getProfileImageUrl()
                if (cachedImageUrl != null) {
                    _profileImageUrl.value = cachedImageUrl
                    Timber.d("ProfileViewModel - Profile image loaded from cache: $cachedImageUrl")
                }

                // 2. 백엔드에서 최신 프로필 정보 가져오기 (현재는 404 예상)
                val result = userRepository.getMyProfile()
                result.onSuccess { profile ->
                    // 백엔드에서 프로필 이미지가 있으면 업데이트
                    if (profile.profileImageUrl != null) {
                        _profileImageUrl.value = profile.profileImageUrl
                        userPreferences.saveProfileImageUrl(profile.profileImageUrl)
                        Timber.d("ProfileViewModel - Profile loaded from backend: ${profile.nickname}")
                    }
                    // 자기소개 업데이트
                    _bio.value = profile.bio ?: ""
                }.onFailure { error ->
                    Timber.w(error, "ProfileViewModel - Failed to load profile from backend: ${error.message}")
                    // 로컬 캐시가 있으면 그대로 사용
                }
            } catch (e: Exception) {
                Timber.e(e, "ProfileViewModel - Exception during profile load")
            }
        }
    }

    /**
     * 내 배지 목록 로드
     */
    fun loadMyBadges() {
        viewModelScope.launch {
            _loadingBadges.value = true
            try {
                // UserPreferences에서 저장된 실제 회원 ID 가져오기
                val currentUserId = userPreferences.getMemberId()

                if (currentUserId != null) {
                    Timber.d("ProfileViewModel - Loading badges for user: $currentUserId")
                    val result = userRepository.getUserBadges(currentUserId)

                    result.onSuccess { badgeList ->
                        _badges.value = badgeList
                        Timber.d("ProfileViewModel - Badges loaded: ${badgeList.size} badges")
                    }.onFailure { error ->
                        Timber.e(error, "ProfileViewModel - Failed to load badges: ${error.message}")
                        _badges.value = emptyList()
                    }
                } else {
                    Timber.w("ProfileViewModel - Member ID not found, cannot load badges")
                    _badges.value = emptyList()
                }
            } catch (e: Exception) {
                Timber.e(e, "ProfileViewModel - Exception during badge load")
                _badges.value = emptyList()
            } finally {
                _loadingBadges.value = false
            }
        }
    }

    /**
     * 특정 사용자의 배지 목록 로드 (다른 사용자 프로필 보기 용)
     * @param userId 조회할 사용자 ID
     */
    fun loadUserBadges(userId: Long) {
        viewModelScope.launch {
            _loadingBadges.value = true
            try {
                Timber.d("ProfileViewModel - Loading badges for user: $userId")
                val result = userRepository.getUserBadges(userId)

                result.onSuccess { badgeList ->
                    _badges.value = badgeList
                    Timber.d("ProfileViewModel - Badges loaded: ${badgeList.size} badges")
                }.onFailure { error ->
                    Timber.e(error, "ProfileViewModel - Failed to load badges: ${error.message}")
                    _badges.value = emptyList()
                }
            } catch (e: Exception) {
                Timber.e(e, "ProfileViewModel - Exception during badge load")
                _badges.value = emptyList()
            } finally {
                _loadingBadges.value = false
            }
        }
    }

    /**
     * 내가 받은 리뷰 목록 로드
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     */
    fun loadMyReviews(page: Int = 0, size: Int = 100) {
        viewModelScope.launch {
            _loadingReviews.value = true
            try {
                // UserPreferences에서 저장된 실제 회원 ID 가져오기
                val currentUserId = userPreferences.getMemberId()

                if (currentUserId != null) {
                    Timber.d("ProfileViewModel - Loading reviews for user: $currentUserId")
                    val result = userRepository.getUserReviews(
                        memberId = currentUserId,
                        page = page,
                        size = size
                    )

                    result.onSuccess { pageResponse ->
                        _reviews.value = pageResponse.content
                        _totalReviews.value = pageResponse.totalElements
                        Timber.d("ProfileViewModel - Reviews loaded: ${pageResponse.content.size} reviews, total: ${pageResponse.totalElements}")
                    }.onFailure { error ->
                        Timber.e(error, "ProfileViewModel - Failed to load reviews: ${error.message}")
                        _reviews.value = emptyList()
                        _totalReviews.value = 0
                    }
                } else {
                    Timber.w("ProfileViewModel - Member ID not found, cannot load reviews")
                    _reviews.value = emptyList()
                    _totalReviews.value = 0
                }
            } catch (e: Exception) {
                Timber.e(e, "ProfileViewModel - Exception during review load")
                _reviews.value = emptyList()
                _totalReviews.value = 0
            } finally {
                _loadingReviews.value = false
            }
        }
    }

    /**
     * 특정 사용자가 받은 리뷰 목록 로드 (다른 사용자 프로필 보기 용)
     * @param userId 조회할 사용자 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     */
    fun loadUserReviews(userId: Long, page: Int = 0, size: Int = 100) {
        viewModelScope.launch {
            _loadingReviews.value = true
            try {
                Timber.d("ProfileViewModel - Loading reviews for user: $userId")
                val result = userRepository.getUserReviews(
                    memberId = userId,
                    page = page,
                    size = size
                )

                result.onSuccess { pageResponse ->
                    _reviews.value = pageResponse.content
                    _totalReviews.value = pageResponse.totalElements
                    Timber.d("ProfileViewModel - Reviews loaded: ${pageResponse.content.size} reviews, total: ${pageResponse.totalElements}")
                }.onFailure { error ->
                    Timber.e(error, "ProfileViewModel - Failed to load reviews: ${error.message}")
                    _reviews.value = emptyList()
                    _totalReviews.value = 0
                }
            } catch (e: Exception) {
                Timber.e(e, "ProfileViewModel - Exception during review load")
                _reviews.value = emptyList()
                _totalReviews.value = 0
            } finally {
                _loadingReviews.value = false
            }
        }
    }

    /**
     * 자기소개 업데이트
     * @param newBio 새로운 자기소개 내용
     * @return 업데이트 성공 여부
     */
    fun updateBio(newBio: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _updatingBio.value = true
            try {
                Timber.d("ProfileViewModel - Updating bio: $newBio")
                val result = userRepository.updateBio(newBio)

                result.onSuccess { profile ->
                    _bio.value = profile.bio ?: ""
                    Timber.d("ProfileViewModel - Bio updated successfully")
                    onSuccess()
                }.onFailure { error ->
                    Timber.e(error, "ProfileViewModel - Failed to update bio: ${error.message}")
                    onError(error.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Timber.e(e, "ProfileViewModel - Exception during bio update")
                onError(e.message ?: "Unknown error")
            } finally {
                _updatingBio.value = false
            }
        }
    }
}
