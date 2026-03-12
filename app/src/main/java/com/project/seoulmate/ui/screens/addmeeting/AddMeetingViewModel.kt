package com.project.seoulmate.ui.screens.addmeeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.seoulmate.data.model.MeetingForm
import com.project.seoulmate.data.repository.MeetingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AddMeetingScreen의 폼 상태와 비즈니스 로직을 담당하는 ViewModel
 * MeetingForm 데이터 클래스 하나의 StateFlow로 통합 관리합니다
 */
@HiltViewModel
class AddMeetingViewModel @Inject constructor(
    private val repository: MeetingRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(MeetingForm())
    val formState: StateFlow<MeetingForm> = _formState.asStateFlow()

    // 저장/등록 결과를 UI에 전달하기 위한 이벤트 상태
    private val _uiEvent = MutableStateFlow<AddMeetingUiEvent?>(null)
    val uiEvent: StateFlow<AddMeetingUiEvent?> = _uiEvent.asStateFlow()

    // ──────────────────────────────────────────
    // 각 필드별 업데이트 함수
    // data class의 copy()를 활용해 특정 필드만 변경합니다
    // ──────────────────────────────────────────

    fun updateMeetingName(name: String) {
        if (name.length <= 10) { // 최대 10자 제한
            _formState.update { it.copy(name = name) }
        }
    }

    fun toggleCategory(category: String) {
        _formState.update { current ->
            val updated = if (current.selectedCategories.contains(category)) {
                current.selectedCategories - category
            } else {
                current.selectedCategories + category
            }
            current.copy(selectedCategories = updated)
        }
    }

    fun addCourse(course: String) {
        _formState.update { it.copy(courses = it.courses + course) }
    }

    fun removeCourse(index: Int) {
        _formState.update { current ->
            current.copy(
                courses = current.courses.filterIndexed { i, _ -> i != index }
            )
        }
    }

    fun addTimeSlot(slot: String) {
        _formState.update { it.copy(timeSlots = it.timeSlots + slot) }
    }

    fun updateDescription(description: String) {
        _formState.update { it.copy(description = description) }
    }

    fun updateExpectedCost(cost: String) {
        _formState.update { it.copy(expectedCost = cost) }
    }

    fun clearExpectedCost() {
        _formState.update { it.copy(expectedCost = "") }
    }

    fun updateMinMembers(min: String) {
        _formState.update { it.copy(minMembers = min) }
    }

    fun updateMaxMembers(max: String) {
        _formState.update { it.copy(maxMembers = max) }
    }

    fun updateIsRepeating(repeating: Boolean) {
        _formState.update { it.copy(isRepeating = repeating) }
    }

    // ──────────────────────────────────────────
    // 저장 / 등록 액션
    // viewModelScope: ViewModel이 살아있는 동안 유지되는 코루틴 스코프
    // ──────────────────────────────────────────

    fun saveDraft() {
        viewModelScope.launch {
            repository.saveMeetingDraft(_formState.value)
            _uiEvent.value = AddMeetingUiEvent.NavigateBack
        }
    }

    fun registerMeeting() {
        viewModelScope.launch {
            repository.registerMeeting(_formState.value)
            _uiEvent.value = AddMeetingUiEvent.NavigateBack
        }
    }

    fun onEventConsumed() {
        _uiEvent.value = null
    }
}

/**
 * UI로 전달되는 일회성 이벤트.
 * (예: 등록 성공 후 화면에서 나가기)
 */
sealed class AddMeetingUiEvent {
    object NavigateBack : AddMeetingUiEvent()
}
