package com.project.seoulmate.data.remote

import com.project.seoulmate.data.model.BlockRequest
import com.project.seoulmate.data.model.BlockResponse
import com.project.seoulmate.data.model.ReportRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * 신고/차단 관련 API 인터페이스
 */
interface UserActionApi {

    /**
     * 신고하기
     * POST /api/reports
     */
    @POST("/api/reports")
    suspend fun report(
        @Header("Authorization") token: String,
        @Body request: ReportRequest
    ): Response<ApiResponse<Unit>>

    /**
     * 차단하기
     * POST /api/blocks
     */
    @POST("/api/blocks")
    suspend fun block(
        @Header("Authorization") token: String,
        @Body request: BlockRequest
    ): Response<ApiResponse<Unit>>

    /**
     * 내 차단 목록 조회
     * GET /api/blocks
     */
    @GET("/api/blocks")
    suspend fun getBlocks(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<BlockResponse>>>
}
