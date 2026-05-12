package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.BlockRequest
import com.project.seoulmate.data.model.BlockResponse
import com.project.seoulmate.data.model.ReportRequest
import com.project.seoulmate.data.remote.UserActionApi
import javax.inject.Inject
import timber.log.Timber

/**
 * UserActionRepository의 실제 구현체.
 */
class UserActionRepositoryImpl @Inject constructor(
    private val userActionApi: UserActionApi
) : UserActionRepository {

    override suspend fun report(token: String, request: ReportRequest): Result<Unit> {
        return try {
            val response = userActionApi.report("Bearer $token", request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val message = response.body()?.message ?: "신고 실패"
                Timber.e("Report failed: $message")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during report")
            Result.failure(e)
        }
    }

    override suspend fun block(token: String, request: BlockRequest): Result<Unit> {
        return try {
            val response = userActionApi.block("Bearer $token", request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val message = response.body()?.message ?: "차단 실패"
                Timber.e("Block failed: $message")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during block")
            Result.failure(e)
        }
    }

    override suspend fun getBlocks(token: String): Result<List<BlockResponse>> {
        return try {
            val response = userActionApi.getBlocks("Bearer $token")
            if (response.isSuccessful && response.body()?.success == true) {
                val blocks = response.body()?.data ?: emptyList()
                Timber.d("Blocked users fetched: ${blocks.size} users")
                Result.success(blocks)
            } else {
                val message = response.body()?.message ?: "차단 목록 조회 실패"
                Timber.e("Get blocks failed: $message")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during get blocks")
            Result.failure(e)
        }
    }
}
