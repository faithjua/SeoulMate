package com.project.seoulmate.ui.screens.home

import androidx.lifecycle.ViewModel
import com.project.seoulmate.data.model.Category
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.repository.MeetingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * HomeScreen의 UI 상태와 비즈니스 로직을 담당하는 ViewModel
 *
 * @HiltViewModel: Hilt가 이 ViewModel을 생성하고 MeetingRepository를 주입
 * @Inject constructor: Hilt에게 MeetingRepository를 요청
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MeetingRepository
) : ViewModel() {

    // StateFlow: 현재 상태를 저장하고, 상태가 바뀌면 수집자(Composable)에게 알림
    // MutableStateFlow: ViewModel 내부에서만 값을 변경할 수 있음 (private)
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    // asStateFlow(): 외부(UI)에는 읽기 전용 StateFlow로만 노출
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _recentMeetings = MutableStateFlow<List<Meeting>>(emptyList())
    val recentMeetings: StateFlow<List<Meeting>> = _recentMeetings.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    // ViewModel이 생성될 때 자동으로 데이터 로드
    init {
        loadData()
    }

    private fun loadData() {
        val categoryList = repository.getCategories()
        _categories.value = categoryList
        // 기본 선택값: "관광" (두 번째 항목, 인덱스 1)
        _selectedCategory.value = categoryList.getOrNull(1)
        _recentMeetings.value = repository.getRecentMeetings()
    }

    /**
     * 카테고리 탭을 선택했을 때 호출.
     * UI에서 직접 상태를 바꾸지 않고, ViewModel 함수를 통해 변경합니다.
     */
    fun onCategorySelected(category: Category) {
        _selectedCategory.update { category }
    }
}
