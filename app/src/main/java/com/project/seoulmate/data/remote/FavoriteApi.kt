package com.project.seoulmate.data.remote

import com.project.seoulmate.data.model.MeetingListResponse
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
        @Query("meetupId") meetupId: String
    ): Response<ApiResponse<Unit>>

    /**
     * 찜 취소
     * DELETE /api/favorites
     */
    @DELETE("/api/favorites")
    suspend fun removeFavorite(
        @Header("Authorization") token: String,
        @Query("meetupId") meetupId: String
    ): Response<ApiResponse<Unit>>

    /**
     * 찜 목록 조회 (페이징)
     * GET /api/favorites
     */
    @GET("/api/favorites")
    suspend fun getFavorites(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PageResponse<MeetingListResponse>>>
}
