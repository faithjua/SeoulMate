package com.project.seoulmate.data.remote

import com.project.seoulmate.data.model.PopularKeywordsResponseData
import com.project.seoulmate.data.model.RecentKeywordsResponseData
import com.project.seoulmate.data.model.RecommendedKeywordsResponseData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * 검색 API 인터페이스
 */
interface SearchApi {

    /**
     * 주간 급상승 검색어 조회
     * GET /api/search/popular
     *
     * weeklyCount 기준 상위 10개
     * 주간 데이터 없으면 누적 카운트로 fallback
     *
     * @return 급상승 검색어 목록 (keyword, weeklyCount, totalCount)
     */
    @GET("/api/search/popular")
    suspend fun getPopularKeywords(): Response<ApiResponse<PopularKeywordsResponseData>>

    /**
     * 최근 검색어 조회 (로그인 사용자)
     * GET /api/search/recent
     *
     * 최대 10개, 비로그인 시 빈 리스트 반환
     *
     * @param token Bearer token (선택, 비로그인 허용)
     * @return 최근 검색어 문자열 목록
     */
    @GET("/api/search/recent")
    suspend fun getRecentKeywords(
        @Header("Authorization") token: String? = null
    ): Response<ApiResponse<RecentKeywordsResponseData>>

    /**
     * 추천 검색어 조회
     * GET /api/search/recommended
     *
     * 로그인 시: 사용자의 관심 카테고리 + 큐레이션 12개 노출
     * 비로그인 시: 큐레이션 12개
     *
     * @param token Bearer token (선택, 비로그인 허용)
     * @return 추천 검색어 문자열 목록
     */
    @GET("/api/search/recommended")
    suspend fun getRecommendedKeywords(
        @Header("Authorization") token: String? = null
    ): Response<ApiResponse<RecommendedKeywordsResponseData>>
}
