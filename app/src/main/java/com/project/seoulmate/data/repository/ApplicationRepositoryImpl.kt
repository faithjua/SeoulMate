package com.project.seoulmate.data.repository

import com.project.seoulmate.data.remote.ApplicationApi
import com.project.seoulmate.data.remote.ApplicationResponse
import javax.inject.Inject
import timber.log.Timber

/**
 * ApplicationRepository의 실제 구현체
 */
class ApplicationRepositoryImpl @Inject constructor(
    private val applicationApi: ApplicationApi
) : ApplicationRepository {

    override suspend fun createApplication(token: String, meetupId: Long): Result<ApplicationResponse> {
        return try {
            val response = applicationApi.createApplication("Bearer $token", meetupId)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Timber.e("Application create success but data is null")
                    Result.failure(Exception("신청 데이터가 없습니다"))
                }
            } else {
                val message = response.body()?.message ?: "신청 실패"
                Timber.e("Create application failed: $message")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during create application")
            Result.failure(e)
        }
    }

    override suspend fun approveApplication(token: String, meetupId: Long, appId: Long): Result<Unit> {
        return try {
            val response = applicationApi.approveApplication("Bearer $token", meetupId, appId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val message = response.body()?.message ?: "승인 실패"
                Timber.e("Approve application failed: $message")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during approve application")
            Result.failure(e)
        }
    }

    override suspend fun rejectApplication(token: String, meetupId: Long, appId: Long): Result<Unit> {
        return try {
            val response = applicationApi.rejectApplication("Bearer $token", meetupId, appId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val message = response.body()?.message ?: "거절 실패"
                Timber.e("Reject application failed: $message")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during reject application")
            Result.failure(e)
        }
    }

    override suspend fun getApplications(token: String, meetupId: Long): Result<List<ApplicationResponse>> {
        return try {
            val response = applicationApi.getApplications("Bearer $token", meetupId)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyList()
                Result.success(data)
            } else {
                val message = response.body()?.message ?: "신청 목록 조회 실패"
                Timber.e("Get applications failed: $message")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during get applications")
            Result.failure(e)
        }
    }

    override suspend fun getMyApplications(token: String): Result<List<ApplicationResponse>> {
        return try {
            val response = applicationApi.getMyApplications("Bearer $token")
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyList()
                Result.success(data)
            } else {
                val message = response.body()?.message ?: "내 신청 목록 조회 실패"
                Timber.e("Get my applications failed: $message")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during get my applications")
            Result.failure(e)
        }
    }
}
