package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

/**
 * 홈 화면 데이터 응답 DTO (ApiResponse의 data 필드에 들어갈 객체)
 */
@Serializable
data class HomeResponseData(
    val meetups: List<HomeMeetingResponse>
)

@Serializable
data class HomeMeetingResponse(
    val id: Long,
    val title: String,
    val imageUrl: String? = null,
    val schedule: String? = null,
    val meetDate: String? = null,
    val maxMembers: Int? = null,
    val minMembers: Int? = null,
    val currentMembers: Int? = null,
    val estimatedCost: Int? = null,
    val status: String? = null,
    val hostNickname: String? = null,
    val congestionLevel: String? = null,
    val congestionLabel: String? = null,
    val isFavorited: Boolean = false,
    val tags: List<String> = emptyList(),
    val region: String? = null
)
