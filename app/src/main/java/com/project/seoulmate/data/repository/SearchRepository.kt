package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.PopularKeyword

/**
 * 검색 Repository 인터페이스
 */
interface SearchRepository {
    /**
     * 주간 급상승 검색어 조회
     * @return 급상승 검색어 목록 (최대 10개)
     */
    suspend fun getPopularKeywords(): Result<List<PopularKeyword>>

    /**
     * 최근 검색어 조회
     * @return 최근 검색어 목록 (최대 10개, 비로그인 시 빈 리스트)
     */
    suspend fun getRecentKeywords(): Result<List<String>>

    /**
     * 추천 검색어 조회
     * @return 추천 검색어 목록 (12개)
     */
    suspend fun getRecommendedKeywords(): Result<List<String>>
}
