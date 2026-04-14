package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

/**
 * 신고 요청 DTO
 * POST /api/reports
 */
@Serializable
data class ReportRequest(
    val targetType: String, // 예: "USER", "MEETUP"
    val targetId: Long,
    val reason: String,
    val description: String
)

/**
 * 차단 요청 DTO
 * POST /api/blocks
 */
@Serializable
data class BlockRequest(
    val blockedUserId: Long
)
