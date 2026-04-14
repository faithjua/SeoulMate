package com.project.seoulmate.ui.screens.addcourse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.BuildConfig
import com.project.seoulmate.data.model.AiCourseRequest
import com.project.seoulmate.data.model.CourseCreateRequest
import com.project.seoulmate.data.model.NaverSearchItem
import com.project.seoulmate.data.remote.NaverSearchApi
import com.project.seoulmate.data.repository.CourseRepository
import com.project.seoulmate.data.model.CourseLocation
import com.project.seoulmate.data.model.CoursePlaceItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class AddCourseViewModel @Inject constructor(
    // Hilt의 NetworkModule이 알아서 이 규격에 맞는 API 객체를 넣어줌
    private val courseRepository: CourseRepository,
    private val naverSearchApi: NaverSearchApi
) : ViewModel() {

    //  1. AI 설명을 담아둘 변수 추가
    private val _aiDescription = MutableStateFlow("")
    val aiDescription: StateFlow<String> = _aiDescription.asStateFlow()
    private val _courseLocations = MutableStateFlow<List<CourseLocation>>(emptyList())
    val courseLocations: StateFlow<List<CourseLocation>> = _courseLocations.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // 네이버 검색 관련 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<NaverSearchItem>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    init {
        // 검색어 디바운싱: 사용자가 입력 후 500ms 동안 멈추면 검색 실행
        viewModelScope.launch {
            _searchQuery
                .debounce(500L)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        searchNaverPlaces(query)
                    } else {
                        _searchResults.value = emptyList()
                    }
                }
        }
    }

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
                //  1. Firebase에서 내 아이디 증명서(토큰) 꺼내기
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
                } else {
                    // 서버 통신 자체가 실패한 경우 (403, 404, 500 에러 등)
                    Timber.tag("AiCourse").e("서버 통신 에러: ${response.code()}")
                }
            } catch (e: Exception) {
                Timber.tag("AiCourse").e("네트워크 에러: ${e.message}")
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
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                //------------------"여기 또 이렇게 추가해줘야해?
                //  1. Firebase에서 내 아이디 증명서(토큰) 꺼내기
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await() // kotlinx-coroutines-play-services 필요, 안되면 리스너 사용
                val idToken = tokenResult?.token

                if (idToken == null) {
                    Timber.tag("AiCourse").e("로그인이 풀렸습니다.")
                    return@launch
                }
                // 1. 현재 뷰모델이 들고 있는 장소 리스트를 백엔드 규격에 맞게 변환
                val placeItems = _courseLocations.value.mapIndexed { index, location ->
                    CoursePlaceItem(
                        placeId = location.placeId,
                        placeName = location.name,
                        address = location.address,
                        latitude = location.lat,
                        longitude = location.lng,
                        orderIndex = index + 1,
                        memo = null
                    )
                }

                // 1-1. 코스 제목 자동 생성: "지역명: 장소1 → 장소2 → 장소3"
                val title = if (_courseLocations.value.isNotEmpty()) {
                    val placeNames = _courseLocations.value.joinToString(" → ") { it.name }
                    "$region: $placeNames"
                } else {
                    "$region 코스"
                }

                // 2. 최종 요청 상자 포장 (Swagger 규격에 맞춰 title 제거)
                val request = CourseCreateRequest(
                    region = region,
                    detailPlace = detailLocation.ifBlank { null },
                    places = placeItems,
                    prompt = originalPrompt.ifBlank { null },
                    aiGenerated = originalPrompt.isNotBlank(), // 프롬프트가 있으면 AI가 만든 것
                    modified = true // 사용자가 중간에 삭제/추가 했는지 판단하는 변수를 별도로 두면 더 좋습니다.
                )

                // 3. 서버로 전송!
                val response = courseRepository.createCourse("Bearer $idToken",request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val savedId = response.body()?.data?.id ?: 1L
                    Timber.tag("CourseSubmit").d("코스 저장 완료! ID: $savedId")
                    onSuccess(savedId)
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

    // 네이버 지도 검색 관련 함수들
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    private suspend fun searchNaverPlaces(query: String) {
        _isSearching.value = true
        try {
            Timber.tag("NaverSearch").d("검색 시작: $query")
            Timber.tag("NaverSearch").d("Client ID 설정 여부: ${BuildConfig.NAVER_CLIENT_ID.isNotEmpty()}")
            Timber.tag("NaverSearch").d("Client Secret 설정 여부: ${BuildConfig.NAVER_CLIENT_SECRET.isNotEmpty()}")

            val response = naverSearchApi.searchLocal(
                clientId = BuildConfig.NAVER_CLIENT_ID,
                clientSecret = BuildConfig.NAVER_CLIENT_SECRET,
                query = query,
                display = 10
            )

            if (response.isSuccessful && response.body() != null) {
                _searchResults.value = response.body()!!.items
                Timber.tag("NaverSearch").d("검색 성공: ${response.body()!!.items.size}개 결과")
            } else {
                Timber.tag("NaverSearch").e("검색 실패: HTTP ${response.code()}")
                Timber.tag("NaverSearch").e("에러 바디: ${response.errorBody()?.string()}")
                _searchResults.value = emptyList()
            }
        } catch (e: Exception) {
            Timber.tag("NaverSearch").e(e, "네이버 검색 에러: ${e.message}")
            _searchResults.value = emptyList()
        } finally {
            _isSearching.value = false
        }
    }

    fun addPlaceFromSearch(item: NaverSearchItem) {
        val newList = _courseLocations.value.toMutableList()
        newList.add(
            CourseLocation(
                name = item.getCleanTitle(),
                lat = item.getLatitude(),
                lng = item.getLongitude(),
                address = item.getBestAddress()
            )
        )
        _courseLocations.value = newList

        // 검색어와 결과 초기화
        clearSearchQuery()

        Timber.tag("AddCourse").d("장소 추가: ${item.getCleanTitle()} (${item.getLatitude()}, ${item.getLongitude()})")
    }
}