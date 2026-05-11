package com.project.seoulmate.data.repository

import com.project.seoulmate.data.remote.NotificationResponse
import com.project.seoulmate.data.remote.PageResponse

/**
 * Notification Repository 인터페이스
 */
interface NotificationRepository {

    /**
     * 알림 목록 조회 (페이징)
     * @param token Authorization token
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param type 알림 타입 필터 (optional)
     */
    suspend fun getNotifications(
        token: String,
        page: Int = 0,
        size: Int = 20,
        type: String? = null
    ): Result<PageResponse<NotificationResponse>>

    /**
     * 알림 읽음 처리
     * @param token Authorization token
     * @param notificationId 알림 ID
     */
    suspend fun markAsRead(token: String, notificationId: Long): Result<Unit>

    /**
     * 모든 알림 읽음 처리
     * @param token Authorization token
     */
    suspend fun markAllAsRead(token: String): Result<Unit>
}
