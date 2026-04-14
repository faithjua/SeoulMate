package com.project.seoulmate.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.repository.MeetingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val repository: MeetingRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
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
}
