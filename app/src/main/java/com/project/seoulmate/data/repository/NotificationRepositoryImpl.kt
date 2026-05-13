package com.project.seoulmate.data.repository

import com.project.seoulmate.data.remote.NotificationApi
import com.project.seoulmate.data.remote.NotificationResponse
import com.project.seoulmate.data.remote.PageResponse
import timber.log.Timber
import javax.inject.Inject

/**
 * NotificationRepository의 실제 구현체
 */
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepository {

    override suspend fun getNotifications(
        token: String,
        page: Int,
        size: Int,
        type: String?
    ): Result<PageResponse<NotificationResponse>> {
        return try {
            Timber.d("Fetching notifications: page=$page, size=$size, type=$type")
            val response = notificationApi.getNotifications("Bearer $token", page, size, type)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Timber.d("Notifications fetched successfully: ${data.content.size} items")
                    Result.success(data)
                } else {
                    Timber.e("Notifications fetch success but data is null")
                    Result.failure(Exception("알림 데이터가 없습니다"))
                }
            } else {
                val message = response.body()?.message ?: "알림 조회 실패"
                Timber.e("Get notifications failed: $message, code=${response.code()}")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during get notifications")
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(token: String, notificationId: Long): Result<Unit> {
        return try {
            Timber.d("Marking notification as read: notificationId=$notificationId")
            val response = notificationApi.markAsRead("Bearer $token", notificationId)

            if (response.isSuccessful && response.body()?.success == true) {
                Timber.d("Notification marked as read successfully")
                Result.success(Unit)
            } else {
                val message = response.body()?.message ?: "읽음 처리 실패"
                Timber.e("Mark as read failed: $message, code=${response.code()}")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during mark as read")
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(token: String): Result<Unit> {
        return try {
            Timber.d("Marking all notifications as read")
            val response = notificationApi.markAllAsRead("Bearer $token")

            if (response.isSuccessful && response.body()?.success == true) {
                Timber.d("All notifications marked as read successfully")
                Result.success(Unit)
            } else {
                val message = response.body()?.message ?: "전체 읽음 처리 실패"
                Timber.e("Mark all as read failed: $message, code=${response.code()}")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during mark all as read")
            Result.failure(e)
        }
    }
}
