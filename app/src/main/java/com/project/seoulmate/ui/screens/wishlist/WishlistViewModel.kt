package com.project.seoulmate.ui.screens.wishlist

import com.project.seoulmate.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.data.model.CategoryItem
import com.project.seoulmate.data.model.CongestionLevelOption
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.repository.CatalogRepository
import com.project.seoulmate.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val meetingRepository: com.project.seoulmate.data.repository.MeetingRepository,
    private val catalogRepository: CatalogRepository,
    private val userActionRepository: com.project.seoulmate.data.repository.UserActionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<Meeting>>(emptyList())
    val uiState: StateFlow<List<Meeting>> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 카탈로그 데이터
    private val _filterCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val filterCategories: StateFlow<List<CategoryItem>> = _filterCategories.asStateFlow()

    private val _congestionLevels = MutableStateFlow<List<CongestionLevelOption>>(emptyList())
    val congestionLevels: StateFlow<List<CongestionLevelOption>> = _congestionLevels.asStateFlow()

    // 차단된 사용자 ID 목록
    private val _blockedUserIds = MutableStateFlow<Set<Long>>(emptySet())
    private val blockedUserIds: StateFlow<Set<Long>> = _blockedUserIds.asStateFlow()

    // 필터 상태
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedCongestion = MutableStateFlow<String?>(null)
    val selectedCongestion: StateFlow<String?> = _selectedCongestion.asStateFlow()

    init {
        loadBlockedUsers()
        loadFavorites()
        loadCatalogData()
    }

    private fun loadCatalogData() {
        viewModelScope.launch {
            // 카테고리 로드
            catalogRepository.getCategories().onSuccess { categories ->
                _filterCategories.value = categories
                Timber.d("Wishlist - Filter categories loaded: ${categories.size} items")
            }.onFailure { error ->
                Timber.e(error, "Wishlist - Failed to load filter categories")
            }

            // 혼잡도 옵션 로드
            catalogRepository.getCongestionLevels().onSuccess { levels ->
                _congestionLevels.value = levels
                Timber.d("Wishlist - Congestion levels loaded: ${levels.size} items")
            }.onFailure { error ->
                Timber.e(error, "Wishlist - Failed to load congestion levels")
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token

                if (idToken == null) {
                    Timber.e("Firebase token is null. Cannot load favorites.")
                    _isLoading.value = false
                    return@launch
                }

                // API 콜 (만남(MEETUP) 타입만 첫 번째 페이지로 20개를 불러옵니다)
                val result = favoriteRepository.getFavorites(
                    token = idToken,
                    targetType = "MEETUP",
                    page = 0,
                    size = 20,
                    category = _selectedCategory.value,
                    congestion = _selectedCongestion.value
                )
                result.onSuccess { pageResponse ->
                    _uiState.value = pageResponse.content.mapNotNull { item ->
                        // 각 찜 항목의 상세 정보 조회 (N+1 쿼리이지만 현재 백엔드 API 구조상 불가피)
                        try {
                            val detailResult = meetingRepository.getMeetingDetail(item.targetId.toString())
                            val detail = detailResult.getOrNull()

                            // 혼잡도 정보를 tags에 추가
                            detail?.let {
                                val congestionLabel = it.mateInfo.name // 임시로 접근 가능한 필드 사용
                                // MeetingDetail에서 혼잡도 정보 추출 필요
                                it.meeting
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to load detail for meeting ${item.targetId}")
                            null
                        }
                    }
                }.onFailure { error ->
                    Timber.e(error, "Failed to load favorites")
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception loading favorites")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 찜 해제 요청
     */
    fun removeFavorite(meetingId: String) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token ?: return@launch

                val result = favoriteRepository.removeFavorite(
                    token = idToken,
                    targetType = "MEETUP",
                    targetId = meetingId.toLongOrNull() ?: 0L
                )

                result.onSuccess {
                    Timber.d("Favorite removed: $meetingId")
                    // 목록에서 즉시 제거하여 반응성 향상
                    _uiState.value = _uiState.value.filter { it.id != meetingId }
                }.onFailure { error ->
                    Timber.e(error, "Failed to remove favorite")
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception removing favorite")
            }
        }
    }

    /**
     * 차단된 사용자 목록 로드
     */
    private fun loadBlockedUsers() {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val token = user?.getIdToken(false)?.await()?.token

                if (token != null) {
                    userActionRepository.getBlocks(token).onSuccess { blocks ->
                        _blockedUserIds.value = blocks.map { it.blockedUserId }.toSet()
                        Timber.d("Blocked users loaded: ${blocks.size} users")
                    }.onFailure { error ->
                        Timber.e(error, "Failed to load blocked users")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading blocked users")
            }
        }
    }

    /**
     * 사용자 차단 후 호출
     * 차단된 사용자 목록을 업데이트하고 찜 목록을 다시 로드합니다.
     *
     * @param blockedUserId 차단된 사용자 ID
     */
    fun onUserBlocked(blockedUserId: Long) {
        viewModelScope.launch {
            // 차단된 사용자 목록에 추가
            _blockedUserIds.update { it + blockedUserId }
            Timber.d("User blocked: $blockedUserId, refreshing wishlist")

            // 찜 목록 다시 로드 (서버에서 차단된 사용자의 만남을 제외하고 반환)
            loadFavorites()
        }
    }

    /**
     * 차단 목록 새로고침
     * 서버로부터 최신 차단 목록을 가져옵니다.
     */
    fun refreshBlockedUsers() {
        loadBlockedUsers()
    }

    /**
     * 카테고리 필터 적용
     * @param categoryCode 선택한 카테고리 code (예: "TOURISM", "KPOP") 또는 null (전체)
     */
    fun applyCategory(categoryCode: String?) {
        _selectedCategory.value = categoryCode
        loadFavorites()
    }

    /**
     * 혼잡도 필터 적용
     * @param congestionCode 선택한 혼잡도 code (예: "RELAXED", "BUSY") 또는 "ALL" (전체)
     */
    fun applyCongestion(congestionCode: String?) {
        // "ALL"은 null로 변환 (전체 선택)
        _selectedCongestion.value = if (congestionCode == "ALL") null else congestionCode
        loadFavorites()
    }
}
