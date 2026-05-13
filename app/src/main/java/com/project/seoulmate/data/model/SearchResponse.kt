package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

/**
 * 검색 API 응답 데이터 모델
 */

/**
 * 주간 급상승 검색어 응답 DTO
 * GET /api/search/popular
 */
@Serializable
data class PopularKeywordsResponseData(
    val keywords: List<PopularKeyword>
)

/**
 * 주간 급상승 검색어 항목
 */
@Serializable
data class PopularKeyword(
    val keyword: String,
    val weeklyCount: Int,
    val totalCount: Int
)

/**
 * 최근 검색어 응답 DTO
 * GET /api/search/recent
 */
@Serializable
data class RecentKeywordsResponseData(
    val keywords: List<String>
)

/**
 * 추천 검색어 응답 DTO
 * GET /api/search/recommended
 */
@Serializable
data class RecommendedKeywordsResponseData(
    val keywords: List<String>
)
