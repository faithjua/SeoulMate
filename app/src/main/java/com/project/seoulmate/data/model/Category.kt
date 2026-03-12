package com.project.seoulmate.data.model

import androidx.annotation.DrawableRes

/**
 * 카테고리 하나를 나타내는 데이터 클래스.
 *
 * @param id 카테고리 고유 식별자
 * @param name 화면에 표시되는 카테고리 이름 (예: "관광", "K-팝")
 * @param iconRes 아이콘 drawable 리소스 ID (null이면 기본 Grid 아이콘 사용)
 * @param isAllMenu true이면 "전체메뉴" 버튼 — 특별한 아이콘을 사용
 */
data class Category(
    val id: String,
    val name: String,
    @DrawableRes val iconRes: Int? = null,
    val isAllMenu: Boolean = false
)
