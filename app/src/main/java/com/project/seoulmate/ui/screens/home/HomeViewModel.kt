package com.project.seoulmate.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.data.model.Category
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.repository.FavoriteRepository
import com.project.seoulmate.data.repository.MeetingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * HomeScreen의 UI 상태와 비즈니스 로직을 담당하는 ViewModel
 *
 * @HiltViewModel: Hilt가 이 ViewModel을 생성하고 MeetingRepository를 주입
 * @Inject constructor: Hilt에게 MeetingRepository를 요청
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MeetingRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    // StateFlow: 현재 상태를 저장하고, 상태가 바뀌면 수집자(Composable)에게 알림
    // MutableStateFlow: ViewModel 내부에서만 값을 변경할 수 있음 (private)
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    // asStateFlow(): 외부(UI)에는 읽기 전용 StateFlow로만 노출
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _recentMeetings = MutableStateFlow<List<Meeting>>(emptyList())
    val recentMeetings: StateFlow<List<Meeting>> = _recentMeetings.asStateFlow()

    private val _todayMeetings = MutableStateFlow<List<Meeting>>(emptyList())
    val todayMeetings: StateFlow<List<Meeting>> = _todayMeetings.asStateFlow()

    private val _lowCongestionMeetings = MutableStateFlow<List<Meeting>>(emptyList())
    val lowCongestionMeetings: StateFlow<List<Meeting>> = _lowCongestionMeetings.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    // ViewModel이 생성될 때 자동으로 데이터 로드
    init {
        loadData()
    }

    private fun loadData() {
        val categoryList = repository.getCategories()
        _categories.value = categoryList
        // 기본 선택값: "전체메뉴" (첫 번째 항목, 인덱스 0)
        val defaultCategory = categoryList.getOrNull(0)
        _selectedCategory.value = defaultCategory
        
        loadHomeData(defaultCategory)
    }

    private fun loadHomeData(category: Category?) {
        viewModelScope.launch {
            // "전체메뉴"이면 null 전달, 다른 카테고리면 name 전달
            val categoryParam = if (category?.isAllMenu == true) null else category?.name
            repository.getHomeData(categoryParam).onSuccess { meetings ->
                _recentMeetings.value = meetings

                // 오늘 날짜의 만남 필터링 (API 24+ 호환)
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val todayMeetings = meetings.filter { meeting ->
                    meeting.meetDate != null && meeting.meetDate.startsWith(today)
                }
                _todayMeetings.value = todayMeetings

                // 혼잡도 낮은 만남 필터링 ("여유" 태그가 있는 만남)
                val lowCongestionMeetings = meetings.filter { meeting ->
                    meeting.tags.any { tag -> tag.contains("여유") }
                }
                _lowCongestionMeetings.value = lowCongestionMeetings

                Timber.d("Home data loaded: ${meetings.size} total, ${todayMeetings.size} today, ${lowCongestionMeetings.size} low congestion")
            }.onFailure { error ->
                Timber.e(error, "Failed to load home data for category: $categoryParam")
                // TODO: 에러 처리 로직 추가 (Toast 등)
            }
        }
    }

    /**
     * 카테고리 탭을 선택했을 때 호출.
     * UI에서 직접 상태를 바꾸지 않고, ViewModel 함수를 통해 변경합니다.
     */
    fun onCategorySelected(category: Category) {
        _selectedCategory.update { category }
        loadHomeData(category)
    }

    /**
     * 찜 추가/제거 토글
     */
    fun toggleFavorite(meetingId: String, currentFavoriteState: Boolean) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val token = user?.getIdToken(false)?.await()?.token

                if (token == null) {
                    Timber.e("User not logged in")
                    return@launch
                }

                val meetingIdLong = meetingId.toLongOrNull() ?: return@launch

                // API 호출
                val result = if (currentFavoriteState) {
                    favoriteRepository.removeFavorite(token, "MEETUP", meetingIdLong)
                } else {
                    favoriteRepository.addFavorite(token, "MEETUP", meetingIdLong)
                }

                result.onSuccess {
                    // 성공 시 UI 상태 업데이트 (isFavorited 토글)
                    val updateMeeting: (Meeting) -> Meeting = { meeting ->
                        if (meeting.id == meetingId) {
                            meeting.copy(isFavorited = !currentFavoriteState)
                        } else {
                            meeting
                        }
                    }

                    _recentMeetings.update { it.map(updateMeeting) }
                    _todayMeetings.update { it.map(updateMeeting) }
                    _lowCongestionMeetings.update { it.map(updateMeeting) }

                    Timber.d("Favorite toggled for meeting $meetingId")
                }.onFailure { error ->
                    Timber.e(error, "Failed to toggle favorite")
                    // TODO: 에러 메시지 UI에 표시
                }
            } catch (e: Exception) {
                Timber.e(e, "Error toggling favorite")
            }
        }
    }
}
