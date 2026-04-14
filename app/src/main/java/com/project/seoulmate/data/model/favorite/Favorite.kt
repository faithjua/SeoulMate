package com.project.seoulmate.data.model.favorite

import kotlinx.serialization.Serializable

/**
 * 찜 추가/취소 요청 DTO
 */
@Serializable
data class FavoriteRequest(
    val targetType: String, // PLACE, MEETUP, COURSE
    val targetId: Long
)

/**
 * 찜 동작(추가/취소) 응답 DTO
 */
@Serializable
data class FavoriteResponse(
    val id: Long,
    val targetType: String,
    val targetId: Long,
    val isFavorited: Boolean? = null,
    val createdAt: String
)

/**
 * 찜 목록 조회 시 개별 항목 DTO
 */
@Serializable
data class FavoriteItemResponse(
    val id: Long,
    val targetType: String,
    val targetId: Long,
    val createdAt: String
)
