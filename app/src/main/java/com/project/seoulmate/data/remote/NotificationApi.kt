package com.project.seoulmate.data.remote

import retrofit2.Response
import retrofit2.http.*
import kotlinx.serialization.Serializable

/**
 * 알림 관련 API 인터페이스
 */
interface NotificationApi {

    /**
     * 알림 목록 조회
     * GET /api/notifications
     *
     * @param token Authorization Bearer token
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param type 알림 타입 필터 (optional)
     * @return 페이징된 알림 목록
     */
    @GET("/api/notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("type") type: String? = null
    ): Response<ApiResponse<PageResponse<NotificationResponse>>>

    /**
     * 알림 읽음 처리
     * PATCH /api/notifications/{notificationId}/read
     */
    @PATCH("/api/notifications/{notificationId}/read")
    suspend fun markAsRead(
        @Header("Authorization") token: String,
        @Path("notificationId") notificationId: Long
    ): Response<ApiResponse<Unit>>

    /**
     * 모든 알림 읽음 처리
     * PATCH /api/notifications/read-all
     */
    @PATCH("/api/notifications/read-all")
    suspend fun markAllAsRead(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Unit>>
}

/**
 * 알림 응답 데이터
 */
@Serializable
data class NotificationResponse(
    val id: Long,
    val type: String, // APPLICATION_RECEIVED, APPLICATION_ACCEPTED, APPLICATION_REJECTED, etc.
    val title: String,
    val message: String,
    val relatedId: Long? = null, // application ID, meetup ID, etc.
    val relatedType: String? = null, // "APPLICATION", "MEETUP", "USER", etc.
    val isRead: Boolean = false,
    val createdAt: String,

    // 알림 관련 추가 정보 (타입에 따라 다름)
    val metadata: NotificationMetadata? = null
)

/**
 * 알림 메타데이터 (타입별 추가 정보)
 */
@Serializable
data class NotificationMetadata(
    // 신청 관련
    val applicationId: Long? = null,
    val meetupId: Long? = null,
    val meetupTitle: String? = null,
    val meetupThumbnailUrl: String? = null,
    val meetDate: String? = null,

    // 사용자 관련
    val userId: Long? = null,
    val userNickname: String? = null,
    val userProfileImage: String? = null,

    // 호스트 관련
    val hostId: Long? = null,
    val hostNickname: String? = null,
    val hostProfileImage: String? = null,

    // 상태
    val status: String? = null // PENDING, ACCEPTED, REJECTED
)
