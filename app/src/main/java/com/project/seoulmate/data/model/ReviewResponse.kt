package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

/**
 * 리뷰 응답 데이터 모델
 * GET /api/users/{memberId}/reviews
 */

/**
 * 리뷰 단일 항목
 *
 * @param id 리뷰 ID
 * @param rating 평점 (1-5)
 * @param content 리뷰 내용
 * @param authorId 작성자 ID
 * @param authorNickname 작성자 닉네임
 * @param authorProfileImage 작성자 프로필 이미지 URL
 * @param targetMemberId 리뷰 대상자 ID
 * @param meetupContextId 만남 ID (컨텍스트)
 * @param meetupContextTitle 만남 제목
 * @param createdAt 작성 시각 (ISO 8601 format)
 */
@Serializable
data class ReviewItem(
    val id: Long,
    val rating: Int,
    val content: String,
    val authorId: Long,
    val authorNickname: String,
    val authorProfileImage: String? = null,
    val targetMemberId: Long,
    val meetupContextId: Long,
    val meetupContextTitle: String,
    val createdAt: String
)
