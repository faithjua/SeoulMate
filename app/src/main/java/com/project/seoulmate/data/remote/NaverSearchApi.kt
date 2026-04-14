package com.project.seoulmate.data.remote

import com.project.seoulmate.data.model.NaverSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * 네이버 Local Search API
 * https://developers.naver.com/docs/serviceapi/search/local/local.md
 */
interface NaverSearchApi {

    @GET("/v1/search/local.json")
    suspend fun searchLocal(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String,
        @Query("display") display: Int = 10,  // 검색 결과 개수 (최대 5)
        @Query("start") start: Int = 1,       // 검색 시작 위치 (최대 1)
        @Query("sort") sort: String = "random" // random(정확도순) 또는 comment(리뷰개수순)
    ): Response<NaverSearchResponse>
}
