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
        @Path("meetupId") meetupId: Long,
        @Body request: ApplicationRequest
    ): Response<ApiResponse<ApplicationResponse>>

    /**
     * 신청 승인 (호스트)
     * POST /api/meetup-applications/{applicationId}/approve
     */
    @POST("/api/meetup-applications/{applicationId}/approve")
    suspend fun approveApplication(
        @Header("Authorization") token: String,
        @Path("applicationId") applicationId: Long
    ): Response<ApiResponse<ApplicationResponse>>

    /**
     * 신청 거절 (호스트)
     * POST /api/meetup-applications/{applicationId}/reject
     */
    @POST("/api/meetup-applications/{applicationId}/reject")
    suspend fun rejectApplication(
        @Header("Authorization") token: String,
        @Path("applicationId") applicationId: Long
    ): Response<ApiResponse<ApplicationResponse>>

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
 * 신청 요청 데이터
 */
@Serializable
data class ApplicationRequest(
    val message: String
)

/**
 * 신청 응답 데이터
 */
@Serializable
data class ApplicationResponse(
    val id: Long,
    val meetupId: Long,
    val meetupTitle: String? = null,
    val meetupThumbnailUrl: String? = null,
    val meetDate: String? = null,
    val applicantId: Long,
    val applicantNickname: String,
    val applicantProfileImage: String? = null,
    val hostId: Long? = null,
    val hostNickname: String? = null,
    val message: String? = null,
    val status: String, // PENDING, ACCEPTED, REJECTED
    val createdAt: String,
    val processedAt: String? = null
)
