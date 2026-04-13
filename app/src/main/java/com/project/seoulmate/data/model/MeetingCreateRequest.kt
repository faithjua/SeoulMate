package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

/**
 * 만남 생성 요청 DTO
 * POST /api/meetups
 */
@Serializable
data class MeetingCreateRequest(
    val title: String,
    val description: String,
    val tags: List<String>,
    val courseId: Long,
    val imageUrls: List<String>,
    val schedule: String,
    val meetDate: String,
    val maxMembers: Int,
    val estimatedCost: Long
)

/**
 * MeetingForm을 MeetingCreateRequest로 변환하는 확장 함수
 */
fun MeetingForm.toCreateRequest(): MeetingCreateRequest {
    return MeetingCreateRequest(
        title = this.name,
        description = this.description,
        tags = this.selectedCategories.toList(),
        courseId = this.courseId ?: 1L, // 기본값 1L (실제로는 코스 생성 후 ID를 받아와야 함)
        imageUrls = this.imageUrls,
        schedule = this.timeSlots.firstOrNull() ?: "",
        meetDate = this.meetDate,
        maxMembers = this.maxMembers.toIntOrNull() ?: 1,
        estimatedCost = this.expectedCost.filter { it.isDigit() }.toLongOrNull() ?: 0L
    )
}
