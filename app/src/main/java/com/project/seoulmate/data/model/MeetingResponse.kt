package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

/**
 * 만남 목록 조회 응답 DTO
 * GET /api/meetups
 */
@Serializable
data class MeetingListResponse(
    val id: Long,
    val title: String,
    val region: String? = null,
    val schedule: String? = null,
    val meetDate: String? = null,
    val maxMembers: Int? = null,
    val currentMembers: Int? = null,
    val estimatedCost: Long? = null,
    val status: String? = null,
    val thumbnailUrl: String? = null,
    val tags: List<String> = emptyList(),
    val hostNickname: String? = null,
    val coursePlaceCount: Int? = null,
    val congestionLevel: String? = null,
    val congestionLabel: String? = null
)

/**
 * 만남 상세 조회 응답 DTO
 * GET /api/meetups/{meetupId}
 */
@Serializable
data class MeetingDetailResponse(
    val id: Long,
    val title: String,
    val description: String,
    val region: String? = null,
    val schedule: String? = null,
    val meetDate: String? = null,
    val maxMembers: Int? = null,
    val currentMembers: Int? = null,
    val estimatedCost: Long? = null,
    val status: String? = null,
    val thumbnailUrl: String? = null,
    val tags: List<String> = emptyList(),
    val hostNickname: String? = null,
    val coursePlaceCount: Int? = null,
    val congestionLevel: String? = null,
    val congestionLabel: String? = null,
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
    val id: Long,
    val nickname: String,
    val profileImageUrl: String? = null,
    val rating: Double? = null,
    val reviewCount: Int = 0,
    val bio: String? = null,
    val isVerified: Boolean = false
)
