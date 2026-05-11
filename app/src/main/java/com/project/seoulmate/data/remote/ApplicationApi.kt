package com.project.seoulmate.data.remote

import retrofit2.Response
import retrofit2.http.*
import kotlinx.serialization.Serializable

/**
 * 만남 신청(Application) 관련 API 인터페이스
 * 백엔드 문서 B-1 참고
 */
interface ApplicationApi {

    /**
     * 만남 신청
     * POST /api/meetups/{meetupId}/applications
     */
    @POST("/api/meetups/{meetupId}/applications")
    suspend fun createApplication(
        @Header("Authorization") token: String,
        @Path("meetupId") meetupId: Long
    ): Response<ApiResponse<ApplicationResponse>>

    /**
     * 신청 승인 (호스트)
     * PATCH /api/meetups/{meetupId}/applications/{appId}/approve
     */
    @PATCH("/api/meetups/{meetupId}/applications/{appId}/approve")
    suspend fun approveApplication(
        @Header("Authorization") token: String,
        @Path("meetupId") meetupId: Long,
        @Path("appId") appId: Long
    ): Response<ApiResponse<Unit>>

    /**
     * 신청 거절 (호스트)
     * PATCH /api/meetups/{meetupId}/applications/{appId}/reject
     */
    @PATCH("/api/meetups/{meetupId}/applications/{appId}/reject")
    suspend fun rejectApplication(
        @Header("Authorization") token: String,
        @Path("meetupId") meetupId: Long,
        @Path("appId") appId: Long
    ): Response<ApiResponse<Unit>>

    /**
     * 신청 목록 조회 (호스트)
     * GET /api/meetups/{meetupId}/applications
     */
    @GET("/api/meetups/{meetupId}/applications")
    suspend fun getApplications(
        @Header("Authorization") token: String,
        @Path("meetupId") meetupId: Long
    ): Response<ApiResponse<List<ApplicationResponse>>>

    /**
     * 내가 보낸 신청 목록 (신청자)
     * GET /api/applications/my
     */
    @GET("/api/applications/my")
    suspend fun getMyApplications(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<ApplicationResponse>>>
}

/**
 * 신청 응답 데이터
 */
@Serializable
data class ApplicationResponse(
    val id: Long,
    val meetupId: Long,
    val applicantId: Long,
    val applicantName: String,
    val applicantProfileUrl: String?,
    val status: String, // PENDING, ACCEPTED, REJECTED
    val createdAt: String
)
