package com.project.seoulmate.data.remote

import com.project.seoulmate.data.model.favorite.FavoriteItemResponse
import com.project.seoulmate.data.model.favorite.FavoriteRequest
import com.project.seoulmate.data.model.favorite.FavoriteResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * 찜(Favorite) 관련 API 인터페이스
 */
interface FavoriteApi {

    /**
     * 찜 추가
     * POST /api/favorites
     */
    @POST("/api/favorites")
    suspend fun addFavorite(
        @Header("Authorization") token: String,
        @Body request: FavoriteRequest
    ): Response<ApiResponse<FavoriteResponse>>

    /**
     * 찜 취소
     * DELETE /api/favorites
     * Retrofit에서 DELETE 바디를 보내려면 @HTTP 어노테이션을 사용해야 합니다.
     */
    @HTTP(method = "DELETE", path = "/api/favorites", hasBody = true)
    suspend fun removeFavorite(
        @Header("Authorization") token: String,
        @Body request: FavoriteRequest
    ): Response<ApiResponse<FavoriteResponse>>

    /**
     * 찜 목록 조회 (페이징)
     * GET /api/favorites
     */
    @GET("/api/favorites")
    suspend fun getFavorites(
        @Header("Authorization") token: String,
        @Query("targetType") targetType: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("category") category: String? = null,
        @Query("congestion") congestion: String? = null
    ): Response<ApiResponse<PageResponse<FavoriteItemResponse>>>
}
