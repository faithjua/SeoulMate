package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

// 요청 상자 (Android -> Spring Boot)
@Serializable
data class AiCourseRequest(
    val date: String,
    val categories: List<String>,
    val members: String, // 인원수
    val budget: String,
    val prompt: String,
    val recommendedStores: List<RecommendedStore> = emptyList(),
    val congestionData: List<CongestionData> = emptyList()
)

/**
 * AI 코스 생성 시 추천 맛집 정보
 */
@Serializable
data class RecommendedStore(
    val shId: String,
    val shName: String,
    val indutyCodeSe: String,
    val indutyCodeSeName: String,
    val shAddr: String,
    val shInfo: String,
    val shPhoto: String,
    val lat: Double,
    val lng: Double
)

/**
 * AI 코스 생성 시 혼잡도 정보
 */
@Serializable
data class CongestionData(
    val areaNm: String,
    val congestionLevel: String,
    val congestionLabel: String,
    val ppltnMin: Int,
    val ppltnMax: Int,
    val observedAt: String
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
    val places: List<CoursePlaceItem>,
    val detailPlace: String? = null,
    val prompt: String? = null,
    val aiGenerated: Boolean? = null,
    val modified: Boolean? = null
)

// 요청용 (POST /api/courses)
@Serializable
data class CoursePlaceItem(
    val name: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val address: String? = null,
    val orderIndex: Int,
    val memo: String? = null
)

// 응답용 (서버 응답 파싱)
@Serializable
data class CoursePlaceInfo(
    val placeId: Long? = null,
    val placeName: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrl: String? = null,
    val orderIndex: Int,
    val memo: String? = null
)

/**
 * 코스 생성/조회 응답 DTO
 */
@Serializable
data class CourseDetailResponse(
    val id: Long,
    val title: String? = null,
    val region: String? = null,
    val detailPlace: String? = null,
    val creator: CourseCreatorInfo? = null,
    val places: List<CoursePlaceInfo> = emptyList(),  // 응답용 DTO 사용
    val createdAt: String? = null
)

@Serializable
data class CourseCreatorInfo(
    val id: Long,
    val nickname: String,
    val profileImage: String? = null
)

/**
 * 코스 목록 조회 응답 DTO (GET /api/courses)
 */
@Serializable
data class CourseListResponse(
    val id: Long,
    val title: String,
    val region: String,
    val placeCount: Int,
    val creatorNickname: String,
    val createdAt: String
)