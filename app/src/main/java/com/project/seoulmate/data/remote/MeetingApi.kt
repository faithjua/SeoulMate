package com.project.seoulmate.data.remote

import com.project.seoulmate.data.model.MeetingCreateRequest
import com.project.seoulmate.data.model.MeetingListResponse
import com.project.seoulmate.data.model.MeetingDetailResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Meeting(만남) 관련 API 인터페이스
 */
interface MeetingApi {

    /**
     * 만남 생성
     * POST /api/meetups
     */
    @POST("/api/meetups")
    suspend fun createMeeting(
        @Header("Authorization") token: String,
        @Body request: MeetingCreateRequest
    ): Response<ApiResponse<MeetingDetailResponse>>

    /**
     * 만남 목록 조회 (페이징)
     * GET /api/meetups
     */
    @GET("/api/meetups")
    suspend fun getMeetings(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("category") category: String? = null
    ): Response<ApiResponse<PageResponse<MeetingListResponse>>>

    /**
     * 만남 상세 조회
     * GET /api/meetups/{meetupId}
     */
    @GET("/api/meetups/{meetupId}")
    suspend fun getMeetingDetail(
        @Path("meetupId") meetupId: String
    ): Response<ApiResponse<MeetingDetailResponse>>
}
