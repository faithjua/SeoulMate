package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

/**
 * 카탈로그 API 응답 데이터 모델
 */

/**
 * 카테고리 단일 항목
 * @param code 카테고리 코드 (영어, 예: "TOURISM", "KPOP")
 * @param label 카테고리 라벨 (한국어, 예: "관광", "K-팝")
 */
@Serializable
data class CategoryItem(
    val code: String,
    val label: String
)

/**
 * 혼잡도 옵션 단일 항목
 * @param code 혼잡도 코드 (영어, 예: "RELAXED", "BUSY")
 * @param label 혼잡도 라벨 (한국어, 예: "여유", "붐빔")
 */
@Serializable
data class CongestionLevelOption(
    val code: String,
    val label: String
)
