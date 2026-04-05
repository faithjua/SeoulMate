package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

// 요청 상자 (Android -> Spring Boot)
@Serializable
data class AiCourseRequest(
    val date: String,
    val categories: List<String>,
    val members: String, // 인원수
    val budget: String,
    val prompt: String
)

// 응답 상자 (Spring Boot -> Android)
@Serializable
data class AiCourseResponse(
    val description: String,
    val places: List<CourseLocation>
)
// 1. 코스 장소 데이터 클래스 (나중에 서버 JSON과 매핑)
@Serializable
data class CourseLocation(
    val name: String,                  // 필수 (수동 추가 시 이름만 넣을 수 있도록)
    val placeId: Long? = null,         // AI 장소나 수동 추가 장소를 위해 Null 허용
    val lat: Double? = null,           // 지오코딩 위도 (선택)
    val lng: Double? = null,           // 지오코딩 경도 (선택)
    val address: String? = null,       // 주소 (선택)
    val congestionLevel: String? = null// 혼잡도 (선택)
)

@Serializable
data class CourseCreateRequest(
    val region: String,
    val detailPlace: String? = null,
    val places: List<CoursePlaceItem>,
    val prompt: String? = null,
    val isAiGenerated: Boolean,
    val isModified: Boolean
)

@Serializable
data class CoursePlaceItem(
    val placeId: Long? = null,  // AI장소는 Null 허용
    val name: String,           // AI 장소는 ID가 없으니 이름과 주소/위경도가 꼭 필요합니다.
    val lat: Double? = null,
    val lng: Double? = null,
    val orderIndex: Int,
    val memo: String? = null
)