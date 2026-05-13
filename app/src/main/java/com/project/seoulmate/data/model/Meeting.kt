package com.project.seoulmate.data.model

import androidx.annotation.DrawableRes

/**
 * 만남(Meeting) 카드 하나를 나타내는 데이터 클래스.
 *
 * @param id 만남 고유 식별자
 * @param title 만남 제목
 * @param time 만남 시간
 * @param price 예상 비용
 * @param rating 평점
 * @param imageRes 대표 이미지 drawable 리소스 ID
 * @param imageUrls 이미지 URL 목록 (여러 장 지원)
 * @param tags 태그 목록 (예: ["혼잡", "#관광", "#한식"])
 * @param meetDate 만남 날짜 (yyyy-MM-dd 형식)
 */
data class Meeting(
    val id: String,
    val title: String,
    val time: String,
    val price: String,
    val rating: String,
    @DrawableRes val imageRes: Int = 0,
    val imageUrls: List<String> = emptyList(),
    val tags: List<String>,
    val isFavorited: Boolean = false,
    val meetDate: String? = null,
    val ratingAvg: Double? = null,
    val maxMembers: Int? = null,
    val currentMembers: Int? = null,
    val status: String? = null
) {
    /**
     * 하위 호환성을 위한 단일 이미지 URL 프로퍼티
     * imageUrls의 첫 번째 이미지를 반환
     */
    val imageUrl: String?
        get() = imageUrls.firstOrNull()
}
