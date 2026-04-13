package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

/**
 * 만남 목록 조회 응답 DTO
 * GET /api/meetups
 */
@Serializable
data class MeetingListResponse(
    val id: String,
    val title: String,
    val meetingTime: String,
    val expectedCost: String? = null,
    val imageUrl: String? = null,
    val categories: List<String> = emptyList(),
    val hostName: String? = null,
    val hostRating: Double? = null,
    val congestionLevel: String? = null
)

/**
 * 만남 상세 조회 응답 DTO
 * GET /api/meetups/{meetupId}
 */
@Serializable
data class MeetingDetailResponse(
    val id: String,
    val title: String,
    val description: String,
    val meetingTime: String,
    val expectedCost: String? = null,
    val minMembers: Int? = null,
    val maxMembers: Int? = null,
    val categories: List<String> = emptyList(),
    val imageUrl: String? = null,
    val location: String? = null,
    val courses: List<CourseResponse> = emptyList(),
    val host: HostInfo? = null,
    val createdAt: String? = null,
    val isFavorite: Boolean = false
)

@Serializable
data class CourseResponse(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val order: Int
)

@Serializable
data class HostInfo(
    val id: String,
    val nickname: String,
    val profileImageUrl: String? = null,
    val rating: Double? = null,
    val reviewCount: Int = 0,
    val bio: String? = null,
    val isVerified: Boolean = false
)
