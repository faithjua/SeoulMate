package com.project.seoulmate.ui.screens.addcourse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.data.model.AiCourseRequest
import com.project.seoulmate.data.model.CourseCreateRequest
import com.project.seoulmate.data.repository.CourseRepository
import com.project.seoulmate.data.model.CourseLocation
import com.project.seoulmate.data.model.CoursePlaceItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddCourseViewModel @Inject constructor(
    // Hilt의 NetworkModule이 알아서 이 규격에 맞는 API 객체를 넣어줌
    private val courseRepository: CourseRepository
) : ViewModel() {


    // AI로 장소 정보 받고, 그 순서 변경시
    private var userHasModified = false

    // AI 코스 생성시 하루 5회 제한 SharedFlow (Toast나 SnackBar용)
    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    //  AI 설명을 담아둘 변수 추가
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
                //  Firebase에서 내 아이디 증명서(토큰) 꺼내기
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await() // kotlinx-coroutines-play-services 필요, 안되면 리스너 사용
                val idToken = tokenResult?.token

                if (idToken == null) {
                    Timber.tag("AiCourse").e("로그인이 풀렸습니다.")
                    return@launch
                }
                //  만남 화면에서 넘겨준 데이터를 코스 화면으로 전달하기 위해 담음
                val requestDto = AiCourseRequest(
                    date = date,
                    categories = categories,
                    members = members,
                    budget = budget,
                    prompt = prompt
                )

                // Hilt로 주입받은 aiApi 사용
                val response = courseRepository.generateAiCourse("Bearer $idToken",requestDto)

                // 1. 통신 성공(HTTP 200) 및 바디(ApiResponse 껍데기)가 null이 아닌지 확인
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!

                    // 2. 서버 비즈니스 로직 성공 여부(success)와 알맹이 데이터(data) null 체크를 동시에!
                    if (apiResponse.success && apiResponse.data != null) {
                        val aiResult = apiResponse.data // 이제 aiResult는 절대 null이 아닙니다!

                        _courseLocations.value = aiResult.places
                        _aiDescription.value = aiResult.description

                        Timber.tag("AiCourse").d("AI 생성 성공: ${aiResult.description}")
                    } else {
                        // 통신은 성공했지만 서버 내부 로직이 실패한 경우 (예: 프롬프트 불량 등)
                        Timber.tag("AiCourse").e("AI 생성 실패: ${apiResponse.message}")
                        // 필요시 _errorMessage.value = apiResponse.message ?: "생성 실패" 등으로 UI에 알려주세요.
                    }
                } else if (response.code() == 429) {
                    // 🚨 429 에러 처리 (횟수 초과)
                    val errorMsg = "하루 AI 생성 횟수(5회)를 초과했습니다. 내일 다시 시도해주세요."
                    _errorEvent.emit(errorMsg)
                    Timber.tag("AiCourse").w(errorMsg)
                } else {
                    // 기타 서버 에러
                    val errorBody = response.errorBody()?.string() // 필요시 에러 바디 파싱
                    _errorEvent.emit("서버 오류가 발생했습니다. (${response.code()})")
                }
            } catch (e: Exception) {
                _errorEvent.emit("네트워크 연결을 확인해주세요.")
            } finally {
                _isAiLoading.value = false
                onComplete()
            }
        }
    }
    fun submitFinalCourse(
        region: String,
        detailLocation: String, // UI의 '세부 장소'
        originalPrompt: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                //  1. Firebase에서 내 아이디 증명서(토큰) 꺼내기
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await() // kotlinx-coroutines-play-services 필요, 안되면 리스너 사용
                val idToken = tokenResult?.token

                if (idToken == null) {
                    Timber.tag("AiCourse").e("로그인이 풀렸습니다.")
                    return@launch
                }
                // 2. 현재 뷰모델이 들고 있는 장소 리스트를 백엔드 규격에 맞게 변환
                val placeItems = _courseLocations.value.mapIndexed { index, location ->
                    CoursePlaceItem(
                        placeId = location.placeId, // 이제 CourseLocation에 placeId가 있어야 합니다.
                        name = location.name,
                        lat = location.lat,         // (선택) CourseLocation에 위도/경도가 있다면 넘겨줍니다.
                        lng = location.lng,         // (선택)
                        orderIndex = index + 1,
                        memo = null                 // 아직 앱 기획에 '장소별 메모' 입력란이 없다면 null로 비워둡니다.
                    )
                }

                // 3. 최종 요청 상자 포장
                val request = CourseCreateRequest(
                    region = region,
                    detailPlace = detailLocation.ifBlank { null },
                    places = placeItems,
                    prompt = originalPrompt,
                    isAiGenerated = originalPrompt.isNotBlank(), // 프롬프트가 있으면 AI가 만든 것
                    isModified = userHasModified //TODO 사용자가 중간에 삭제/추가 했는지 판단하는 변수 private var userHasModified = false 추가하기
                )

                // 4. 서버로 전송!
                val response = courseRepository.createCourse("Bearer $idToken",request)

                if (response.isSuccessful && response.body()?.success == true) {
                    Timber.tag("CourseSubmit").d("코스 저장 완료!")
                    onSuccess()
                } else {
                    val errorMsg = response.body()?.message ?: "코스 저장 실패"
                    Timber.tag("CourseSubmit").e(errorMsg)
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                Timber.tag("CourseSubmit").e(e, "네트워크 에러")
                onError("네트워크 에러가 발생했습니다.")
            }
        }
    }
/*
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
    fun moveLocation(fromIndex: Int, toIndex: Int) {
        val newList = _courseLocations.value.toMutableList()
        if (fromIndex in newList.indices && toIndex in newList.indices) {
            // 두 아이템의 위치를 바꿈 (Swap)
            val temp = newList[fromIndex]
            newList[fromIndex] = newList[toIndex]
            newList[toIndex] = temp

            _courseLocations.value = newList
        }
    }

 */

    // 1. 개선된 moveLocation (Reorder 방식)
    fun moveLocation(fromIndex: Int, toIndex: Int) {
        val newList = _courseLocations.value.toMutableList()
        if (fromIndex in newList.indices && toIndex in newList.indices) {
            // 끼워넣기 로직
            val item = newList.removeAt(fromIndex)
            newList.add(toIndex, item)

            _courseLocations.value = newList
            userHasModified = true // 사용자가 순서를 바꿨으므로 수정됨으로 표시
        }
    }

    // 2. 수정된 추가/삭제 로직 (플래그 반영)
    fun addLocationManual(name: String) {
        if (name.isNotBlank()) {
            val newList = _courseLocations.value.toMutableList()
            newList.add(CourseLocation(name = name))
            _courseLocations.value = newList
            userHasModified = true // 수동 추가 발생
        }
    }

    fun removeLocation(index: Int) {
        val newList = _courseLocations.value.toMutableList()
        if (index in newList.indices) {
            newList.removeAt(index)
            _courseLocations.value = newList
            userHasModified = true // 수동 삭제 발생
        }
    }

}