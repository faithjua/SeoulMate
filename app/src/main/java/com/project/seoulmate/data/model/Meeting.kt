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
 * @param tags 태그 목록 (예: ["혼잡", "#관광", "#한식"])
 */
data class Meeting(
    val id: String,
    val title: String,
    val time: String,
    val price: String,
    val rating: String,
    @DrawableRes val imageRes: Int,
    val tags: List<String>
)
