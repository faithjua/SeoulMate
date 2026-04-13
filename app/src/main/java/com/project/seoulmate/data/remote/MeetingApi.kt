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
        @Query("status") status: String? = null,
        @Query("keyword") keyword: String? = null
    ): Response<ApiResponse<PageResponse<MeetingListResponse>>>

    /**
     * 만남 상세 조회
     * GET /api/meetups/{meetupId}
     */
    @GET("/api/meetups/{meetupId}")
    suspend fun getMeetingDetail(
        @Path("meetupId") meetupId: Long
    ): Response<ApiResponse<MeetingDetailResponse>>

    /**
     * 만남 참가
     */
    @POST("/api/meetups/{meetupId}/join")
    suspend fun joinMeeting(
        @Header("Authorization") token: String,
        @Path("meetupId") meetupId: Long
    ): Response<ApiResponse<Unit>>

    /**
     * 만남 참가 취소
     */
    @DELETE("/api/meetups/{meetupId}/leave")
    suspend fun leaveMeeting(
        @Header("Authorization") token: String,
        @Path("meetupId") meetupId: Long
    ): Response<ApiResponse<Unit>>

    /**
     * 만남 상태 변경 (PATCH)
     */
    @PATCH("/api/meetups/{meetupId}/status")
    suspend fun updateMeetingStatus(
        @Header("Authorization") token: String,
        @Path("meetupId") meetupId: Long,
        @Query("status") status: String
    ): Response<ApiResponse<Unit>>
}
