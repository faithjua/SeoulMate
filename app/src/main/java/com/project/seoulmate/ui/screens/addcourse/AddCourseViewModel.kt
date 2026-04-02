package com.project.seoulmate.ui.screens.addcourse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.seoulmate.signup.AiApi
import com.project.seoulmate.signup.AiCourseRequest
import com.project.seoulmate.signup.CourseLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCourseViewModel @Inject constructor(
    // Hilt의 NetworkModule이 알아서 이 규격에 맞는 API 객체를 넣어줌
    private val aiApi: AiApi
) : ViewModel() {

    //  1. AI 설명을 담아둘 변수 추가
    private val _aiDescription = MutableStateFlow("")
    val aiDescription: StateFlow<String> = _aiDescription.asStateFlow()
    private val _courseLocations = MutableStateFlow<List<CourseLocation>>(emptyList())
    val courseLocations: StateFlow<List<CourseLocation>> = _courseLocations.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // 더미값을 빼고 date와 categories를 파라미터로 직접 받음
    fun generateCourseFromAi(
        date: String,
        categories: List<String>,
        members: String,
        budget: String,
        prompt: String,
        onComplete: () -> Unit
    ) {
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                //  만남 화면에서 넘겨준 데이터를 코스 화면으로 전달하기 위해 담음
                val requestDto = AiCourseRequest(
                    date = date,
                    categories = categories,
                    members = members,
                    budget = budget,
                    prompt = prompt
                )

                // Hilt로 주입받은 aiApi 사용
                val response = aiApi.generateAiCourse(requestDto)

                if (response.isSuccessful) {
                    response.body()?.let { aiResult ->
                        _courseLocations.value = aiResult.places
                        //  통신 성공 시 설명(description)도 뷰모델에 저장!
                        _aiDescription.value = aiResult.description
                        Log.d("AiCourse", "AI 생성 성공: ${aiResult.description}")
                    }
                } else {
                    Log.e("AiCourse", "서버 에러: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("AiCourse", "네트워크 에러: ${e.message}")
            } finally {
                _isAiLoading.value = false
                onComplete()
            }
        }
    }

    fun addLocationManual(name: String) {
        if (name.isNotBlank()) {
            val newList = _courseLocations.value.toMutableList()
            newList.add(CourseLocation(name = name))
            _courseLocations.value = newList
        }
    }

    fun removeLocation(index: Int) {
        val newList = _courseLocations.value.toMutableList()
        if (index in newList.indices) {
            newList.removeAt(index)
            _courseLocations.value = newList
        }
    }
}