package com.project.seoulmate.ui.screens.wishlist

import com.project.seoulmate.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val meetingRepository: com.project.seoulmate.data.repository.MeetingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<Meeting>>(emptyList())
    val uiState: StateFlow<List<Meeting>> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadFavorites()
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
                val result = favoriteRepository.getFavorites(idToken, targetType = "MEETUP", page = 0, size = 20)
                result.onSuccess { pageResponse ->
                    _uiState.value = pageResponse.content.mapNotNull { item ->
                        // 각 찜 항목의 상세 정보 조회 (N+1 쿼리이지만 현재 백엔드 API 구조상 불가피)
                        try {
                            val detailResult = meetingRepository.getMeetingDetail(item.targetId.toString())
                            detailResult.getOrNull()?.meeting
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
}
