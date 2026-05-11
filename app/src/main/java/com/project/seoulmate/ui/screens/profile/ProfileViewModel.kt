package com.project.seoulmate.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.repository.MeetingRepository
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
    private val userPreferences: com.project.seoulmate.data.local.UserPreferences
) : ViewModel() {

    private val _allMeetings = MutableStateFlow<List<Meeting>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _totalMeetings = MutableStateFlow(0)
    val totalMeetings: StateFlow<Int> = _totalMeetings.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val myMeetings: StateFlow<List<Meeting>> = combine(_allMeetings, _searchQuery) { meetings, query ->
        if (query.isBlank()) {
            meetings
        } else {
            meetings.filter { it.title.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadMyMeetings()
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
}
