package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

/**
 * 카탈로그 API 응답 데이터 모델
 */

/**
 * 카테고리 목록 응답
 */
@Serializable
data class CategoriesResponseData(
    val data: List<String>
)

/**
 * 혼잡도 옵션 응답
 */
@Serializable
data class CongestionLevelsResponseData(
    val data: List<CongestionLevelOption>
)

/**
 * 혼잡도 옵션 단일 항목
 */
@Serializable
data class CongestionLevelOption(
    val code: String,
    val label: String
)
