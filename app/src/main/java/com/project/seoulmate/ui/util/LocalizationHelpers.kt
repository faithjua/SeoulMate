package com.project.seoulmate.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.project.seoulmate.R

/**
 * Localization helpers.
 *
 * Backend의 카테고리/혼잡도 "type"은 한글 문자열을 그대로 사용한다 (예: "관광", "여유").
 * 화면에 표시할 때만 이 헬퍼들을 통해 현재 locale에 맞는 라벨로 변환한다.
 * 백엔드로 전송하거나 비교할 때는 항상 원본 한글 값을 사용해야 한다.
 */

/** Backend가 주는 한글 카테고리 이름을 화면 표시용 텍스트로 변환. */
@Composable
fun displayCategoryName(name: String): String = when (name) {
    "전체메뉴" -> stringResource(R.string.category_all_menu)
    "당일만남" -> stringResource(R.string.category_daily)
    "관광" -> stringResource(R.string.category_tourism)
    "K-팝" -> stringResource(R.string.category_kpop)
    "K-뷰티" -> stringResource(R.string.category_kbeauty)
    "쇼핑" -> stringResource(R.string.category_shopping)
    "한식" -> stringResource(R.string.category_food)
    "카페" -> stringResource(R.string.category_cafe)
    "교통 가이드", "교통가이드" -> stringResource(R.string.category_transport_guide)
    "클래스" -> stringResource(R.string.category_class)
    "커뮤니티" -> stringResource(R.string.category_community)
    "전시/공연", "전시·스타일" -> stringResource(R.string.category_exhibition)
    "안전/생활", "안전·생활" -> stringResource(R.string.category_safety_life)
    "숙소/지역" -> stringResource(R.string.category_accommodation)
    else -> name
}

/** Backend가 주는 한글 혼잡도 라벨을 화면 표시용 텍스트로 변환. */
@Composable
fun displayCongestionLabel(label: String): String = when (label) {
    "전체" -> stringResource(R.string.wishlist_filter_all)
    "여유" -> stringResource(R.string.home_congestion_free)
    "보통" -> stringResource(R.string.home_congestion_normal)
    "약간 붐빔" -> stringResource(R.string.home_congestion_slightly_crowded)
    "붐빔", "혼잡" -> stringResource(R.string.home_congestion_crowded)
    "정보 없음" -> stringResource(R.string.congestion_unknown)
    else -> label
}

/** 태그가 혼잡도 라벨인지 여부. # 접두사는 떼고 비교한다. */
fun isCongestionTag(rawTag: String): Boolean {
    val clean = rawTag.removePrefix("#")
    return clean in CONGESTION_LABELS
}

/** 태그에 사용할 색상. 혼잡도면 등급별 색, 카테고리면 기본 보라색. */
fun tagColor(rawTag: String): Color {
    val clean = rawTag.removePrefix("#")
    return when (clean) {
        "여유" -> Color(0xFF6CF0A0)
        "보통" -> Color(0xFF4A90E2)
        "약간 붐빔" -> Color(0xFFFF9500)
        "붐빔", "혼잡" -> Color(0xFFFF6B6B)
        "정보 없음" -> Color(0xFF9E9E9E)
        else -> Color(0xFF6C60FD)
    }
}

/**
 * Meeting.tags의 원본 한글 태그를 화면에 표시할 텍스트로 변환.
 * 혼잡도면 그대로 (번역된) 라벨, 카테고리면 "#카테고리".
 */
@Composable
fun displayMeetingTag(rawTag: String): String {
    val clean = rawTag.removePrefix("#")
    return if (clean in CONGESTION_LABELS) {
        displayCongestionLabel(clean)
    } else {
        "#" + displayCategoryName(clean)
    }
}

private val CONGESTION_LABELS = setOf("여유", "보통", "약간 붐빔", "붐빔", "혼잡", "정보 없음")