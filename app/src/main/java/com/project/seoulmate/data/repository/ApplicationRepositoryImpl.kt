package com.project.seoulmate.data.repository

import com.project.seoulmate.data.remote.ApplicationApi
import com.project.seoulmate.data.remote.ApplicationRequest
import com.project.seoulmate.data.remote.ApplicationResponse
import javax.inject.Inject
import timber.log.Timber

/**
 * ApplicationRepository의 실제 구현체
 */
class ApplicationRepositoryImpl @Inject constructor(
    private val applicationApi: ApplicationApi
) : ApplicationRepository {

    override suspend fun createApplication(token: String, meetupId: Long, message: String): Result<ApplicationResponse> {
        return try {
            val request = ApplicationRequest(message = message)
            val response = applicationApi.createApplication("Bearer $token", meetupId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Timber.d("Application created successfully: id=${data.id}, status=${data.status}")
                    Result.success(data)
                } else {
                    Timber.e("Application create success but data is null")
                    Result.failure(Exception("신청 데이터가 없습니다"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "신청 실패"
                Timber.e("Create application failed: $errorMessage, code=${response.code()}")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during create application")
            Result.failure(e)
        }
    }

    override suspend fun approveApplication(token: String, applicationId: Long): Result<ApplicationResponse> {
        return try {
            Timber.d("Approving application: applicationId=$applicationId")
            val response = applicationApi.approveApplication("Bearer $token", applicationId)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Timber.d("Application approved: id=${data.id}, status=${data.status}")
                    Result.success(data)
                } else {
                    Timber.e("Approve success but data is null")
                    Result.failure(Exception("승인 데이터가 없습니다"))
                }
            } else {
                val message = response.body()?.message ?: "승인 실패"
                Timber.e("Approve application failed: $message, code=${response.code()}")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during approve application")
            Result.failure(e)
        }
    }

    override suspend fun rejectApplication(token: String, applicationId: Long): Result<ApplicationResponse> {
        return try {
            Timber.d("Rejecting application: applicationId=$applicationId")
            val response = applicationApi.rejectApplication("Bearer $token", applicationId)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Timber.d("Application rejected: id=${data.id}, status=${data.status}")
                    Result.success(data)
                } else {
                    Timber.e("Reject success but data is null")
                    Result.failure(Exception("거절 데이터가 없습니다"))
                }
            } else {
                val message = response.body()?.message ?: "거절 실패"
                Timber.e("Reject application failed: $message, code=${response.code()}")
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
