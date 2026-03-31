package com.project.seoulmate.signup


import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName



@Serializable
data class LoginRequest(
    val email: String,
    val nickname: String,
    val role: String,
    val nationality: String
)

@Serializable
data class MemberResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    //val isNewMember: Boolean? = null,
    //서버가 안주거나 못찾으면 기존유저로 간주하는것이 정말 안전한가?
    @SerialName("newMember")
    val isNewMember: Boolean = false,
    val role: String,
    val nationality: String
)

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
    val places: List<CourseLocation> // 기존에 만드신 클래스 재활용!
)
// 1. 코스 장소 데이터 클래스 (나중에 서버 JSON과 매핑될 녀석입니다)
@Serializable
data class CourseLocation(
    val name: String,
    val lat: Double = 0.0,
    val lng: Double = 0.0
)