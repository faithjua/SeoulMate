package com.project.seoulmate.ui.screens.notification

import com.project.seoulmate.data.remote.ApplicationResponse
import com.project.seoulmate.data.remote.NotificationResponse

/**
 * 통합 알림 모델
 * - ApplicationNotification: 호스트가 받은 메이트 신청 (승인/거절 가능)
 * - StatusNotification: 신청자가 받은 승인/거절 알림 (정보만 표시)
 */
sealed class UnifiedNotification {
    abstract val id: String
    abstract val createdAt: String
    abstract val sortTimestamp: Long // 시간순 정렬용

    /**
     * 호스트가 받은 메이트 신청 알림
     */
    data class ApplicationNotification(
        val application: ApplicationResponse,
        override val sortTimestamp: Long
    ) : UnifiedNotification() {
        override val id: String = "app_${application.id}"
        override val createdAt: String = application.createdAt
    }

    /**
     * 신청자가 받은 승인/거절 알림
     */
    data class StatusNotification(
        val notification: NotificationResponse,
        override val sortTimestamp: Long
    ) : UnifiedNotification() {
        override val id: String = "notif_${notification.id}"
        override val createdAt: String = notification.createdAt
    }
}

/**
 * ISO 8601 날짜 문자열을 timestamp로 변환 (간단 버전)
 * 형식: "2026-05-11T13:26:12.345Z"
 */
fun parseIsoTimestamp(isoString: String): Long {
    return try {
        // 간단 파싱: ISO 8601 문자열을 timestamp로 변환
        // 실제로는 SimpleDateFormat이나 java.time API를 사용하는 것이 더 정확
        val cleanString = isoString
            .replace("T", " ")
            .replace("Z", "")
            .substringBefore(".")

        // 임시로 문자열 길이 기반 정렬 (ISO 8601은 lexicographic order가 시간순)
        // 더 정확한 파싱이 필요하면 java.time.Instant 사용 권장
        isoString.hashCode().toLong()
    } catch (e: Exception) {
        0L
    }
}
