package com.project.seoulmate.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.model.PopularKeyword
import com.project.seoulmate.data.repository.MeetingRepository
import com.project.seoulmate.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val meetings: List<Meeting>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MeetingRepository,
    private val searchRepository: SearchRepository,
    private val userActionRepository: com.project.seoulmate.data.repository.UserActionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // 주간 급상승 검색어
    private val _popularKeywords = MutableStateFlow<List<PopularKeyword>>(emptyList())
    val popularKeywords = _popularKeywords.asStateFlow()

    // 최근 검색어
    private val _recentKeywords = MutableStateFlow<List<String>>(emptyList())
    val recentKeywords = _recentKeywords.asStateFlow()

    // 추천 검색어
    private val _recommendedKeywords = MutableStateFlow<List<String>>(emptyList())
    val recommendedKeywords = _recommendedKeywords.asStateFlow()

    // 차단된 사용자 ID 목록
    private val _blockedUserIds = MutableStateFlow<Set<Long>>(emptySet())
    private val blockedUserIds: StateFlow<Set<Long>> = _blockedUserIds.asStateFlow()

    init {
        // 차단 목록 로드
        loadBlockedUsers()

        // 검색어 API 로드
        loadPopularKeywords()
        loadRecentKeywords()
        loadRecommendedKeywords()

        // 검색어 디바운싱: 사용자가 입력 후 500ms 동안 멈추면 검색 실행
        viewModelScope.launch {
            _searchQuery
                .debounce(500L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.update { SearchUiState.Idle }
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearQuery() {
        _searchQuery.value = ""
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { SearchUiState.Loading }
        try {
            // 현재 무한 스크롤이 아니므로 넉넉하게 50개를 불러옵니다.
            val result = repository.getMeetings(page = 0, size = 50, keyword = query)
            result.onSuccess { pageResponse ->
                _uiState.update { SearchUiState.Success(pageResponse.content) }
            }.onFailure { error ->
                handleError(error)
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun handleError(error: Throwable) {
        Timber.e(error, "Search fetch failed")
        val message = when (error) {
            is SocketTimeoutException -> "서버 응답 시간이 초과되었습니다. 다시 시도해주세요."
            is IOException -> "네트워크 문제가 발생했습니다. 연결 상태를 확인해주세요."
            is HttpException -> {
                when (error.code()) {
                    in 500..599 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    else -> "알 수 없는 에러가 발생했습니다. (Code: ${error.code()})"
                }
            }
            else -> error.message ?: "데이터를 불러오는 중 오류가 발생했습니다."
        }
        _uiState.update { SearchUiState.Error(message) }
    }

    /**
     * 주간 급상승 검색어 로드
     */
    private fun loadPopularKeywords() {
        viewModelScope.launch {
            searchRepository.getPopularKeywords().onSuccess { keywords ->
                _popularKeywords.value = keywords
                Timber.d("Popular keywords loaded: ${keywords.size} items")
            }.onFailure { error ->
                Timber.e(error, "Failed to load popular keywords")
                _popularKeywords.value = emptyList()
            }
        }
    }

    /**
     * 최근 검색어 로드
     */
    fun loadRecentKeywords() {
        viewModelScope.launch {
            searchRepository.getRecentKeywords().onSuccess { keywords ->
                _recentKeywords.value = keywords
                Timber.d("Recent keywords loaded: ${keywords.size} items")
            }.onFailure { error ->
                Timber.e(error, "Failed to load recent keywords")
                _recentKeywords.value = emptyList()
            }
        }
    }

    /**
     * 추천 검색어 로드
     */
    private fun loadRecommendedKeywords() {
        viewModelScope.launch {
            searchRepository.getRecommendedKeywords().onSuccess { keywords ->
                _recommendedKeywords.value = keywords
                Timber.d("Recommended keywords loaded: ${keywords.size} items")
            }.onFailure { error ->
                Timber.e(error, "Failed to load recommended keywords")
                _recommendedKeywords.value = emptyList()
            }
        }
    }

    /**
     * 검색어 클릭 시 호출 (검색 실행)
     */
    fun onKeywordClick(keyword: String) {
        _searchQuery.value = keyword
    }

    /**
     * 차단된 사용자 목록 로드
     */
    private fun loadBlockedUsers() {
        viewModelScope.launch {
            try {
                val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
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
     * 차단된 사용자 목록을 업데이트하고 검색 결과를 다시 로드합니다.
     *
     * @param blockedUserId 차단된 사용자 ID
     */
    fun onUserBlocked(blockedUserId: Long) {
        viewModelScope.launch {
            // 차단된 사용자 목록에 추가
            _blockedUserIds.update { it + blockedUserId }
            Timber.d("User blocked: $blockedUserId, refreshing search results")

            // 현재 검색어로 다시 검색 (서버에서 차단된 사용자의 만남을 제외하고 반환)
            val currentQuery = _searchQuery.value
            if (currentQuery.isNotBlank()) {
                performSearch(currentQuery)
            }
        }
    }

    /**
     * 차단 목록 새로고침
     * 서버로부터 최신 차단 목록을 가져옵니다.
     */
    fun refreshBlockedUsers() {
        loadBlockedUsers()
    }
}
