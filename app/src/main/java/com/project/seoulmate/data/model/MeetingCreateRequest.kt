package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

/**
 * 만남 생성/수정 요청 DTO
 * POST /api/meetups (생성)
 * PATCH /api/meetups/{meetupId} (수정)
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
    val minMembers: Int,
    val maxMembers: Int,
    val estimatedCost: Long,
    val publish: Boolean = true,
    val repeat: Boolean = false
)

/**
 * MeetingForm을 MeetingCreateRequest로 변환하는 확장 함수
 */
fun MeetingForm.toCreateRequest(publish: Boolean = true, repeat: Boolean = false): MeetingCreateRequest {
    return MeetingCreateRequest(
        title = this.name,
        description = this.description,
        // tags: #을 제거하고 공백 정리 (예: "#관광" → "관광", "# K-팝" → "K-팝")
        tags = this.selectedCategories.map { it.removePrefix("#").trim() },
        courseId = this.courseId ?: 1L, // 기본값 1L (실제로는 코스 생성 후 ID를 받아와야 함)
        imageUrls = this.imageUrls,
        schedule = this.timeSlots.firstOrNull() ?: "",
        meetDate = this.meetDate,
        minMembers = this.minMembers.toIntOrNull() ?: 1,
        maxMembers = this.maxMembers.toIntOrNull() ?: 1,
        estimatedCost = this.expectedCost.filter { it.isDigit() }.toLongOrNull() ?: 0L,
        publish = publish,
        repeat = repeat
    )
}
