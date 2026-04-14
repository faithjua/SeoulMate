package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.favorite.FavoriteItemResponse
import com.project.seoulmate.data.model.favorite.FavoriteRequest
import com.project.seoulmate.data.remote.FavoriteApi
import com.project.seoulmate.data.remote.PageResponse
import timber.log.Timber
import javax.inject.Inject

/**
 * FavoriteRepository의 실제 구현체
 */
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteApi: FavoriteApi
) : FavoriteRepository {

    /**
     * 찜 추가
     */
    override suspend fun addFavorite(token: String, targetType: String, targetId: Long): Result<Unit> {
        return try {
            val request = FavoriteRequest(targetType, targetId)
            val response = favoriteApi.addFavorite("Bearer $token", request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: "찜 추가 실패"
                Timber.e("Add favorite failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Add favorite error")
            Result.failure(e)
        }
    }

    /**
     * 찜 취소
     */
    override suspend fun removeFavorite(token: String, targetType: String, targetId: Long): Result<Unit> {
        return try {
            val request = FavoriteRequest(targetType, targetId)
            val response = favoriteApi.removeFavorite("Bearer $token", request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: "찜 취소 실패"
                Timber.e("Remove favorite failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Remove favorite error")
            Result.failure(e)
        }
    }

    /**
     * 찜 목록 조회 (페이징)
     */
    override suspend fun getFavorites(
        token: String,
        targetType: String?,
        page: Int,
        size: Int
    ): Result<PageResponse<FavoriteItemResponse>> {
        return try {
            val response = favoriteApi.getFavorites("Bearer $token", targetType, page, size)

            if (response.isSuccessful && response.body()?.success == true) {
                val pageData = response.body()?.data
                if (pageData != null) {
                    Result.success(pageData)
                } else {
                    Result.failure(Exception("응답 데이터가 없습니다"))
                }
            } else {
                val errorMsg = response.body()?.message ?: "찜 목록 조회 실패"
                Timber.e("Get favorites failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Get favorites error")
            Result.failure(e)
        }
    }
}
