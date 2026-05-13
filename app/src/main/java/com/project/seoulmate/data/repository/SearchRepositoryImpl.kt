package com.project.seoulmate.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.data.model.PopularKeyword
import com.project.seoulmate.data.remote.SearchApi
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * SearchRepository 구현체
 */
class SearchRepositoryImpl @Inject constructor(
    private val searchApi: SearchApi,
    private val auth: FirebaseAuth
) : SearchRepository {

    override suspend fun getPopularKeywords(): Result<List<PopularKeyword>> {
        return try {
            Timber.d("Fetching popular keywords")
            val response = searchApi.getPopularKeywords()

            if (response.isSuccessful && response.body()?.success == true) {
                val keywords = response.body()?.data?.keywords ?: emptyList()
                Timber.d("Popular keywords fetched: ${keywords.size} items")
                Result.success(keywords)
            } else {
                val errorMsg = response.body()?.message ?: "Unknown error"
                Timber.e("Popular keywords fetch failed: ${response.code()}, message: $errorMsg")
                Result.failure(Exception("Failed to fetch popular keywords: $errorMsg"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch popular keywords")
            Result.failure(e)
        }
    }

    override suspend fun getRecentKeywords(): Result<List<String>> {
        return try {
            // 토큰 가져오기 (비로그인 허용)
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
            val authHeader = token?.let { "Bearer $it" }

            Timber.d("Fetching recent keywords (auth: ${authHeader != null})")
            val response = searchApi.getRecentKeywords(token = authHeader)

            if (response.isSuccessful && response.body()?.success == true) {
                val keywords = response.body()?.data?.keywords ?: emptyList()
                Timber.d("Recent keywords fetched: ${keywords.size} items")
                Result.success(keywords)
            } else {
                val errorMsg = response.body()?.message ?: "Unknown error"
                Timber.e("Recent keywords fetch failed: ${response.code()}, message: $errorMsg")
                Result.failure(Exception("Failed to fetch recent keywords: $errorMsg"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch recent keywords")
            Result.failure(e)
        }
    }

    override suspend fun getRecommendedKeywords(): Result<List<String>> {
        return try {
            // 토큰 가져오기 (비로그인 허용)
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
            val authHeader = token?.let { "Bearer $it" }

            Timber.d("Fetching recommended keywords (auth: ${authHeader != null})")
            val response = searchApi.getRecommendedKeywords(token = authHeader)

            if (response.isSuccessful && response.body()?.success == true) {
                val keywords = response.body()?.data?.keywords ?: emptyList()
                Timber.d("Recommended keywords fetched: ${keywords.size} items")
                Result.success(keywords)
            } else {
                val errorMsg = response.body()?.message ?: "Unknown error"
                Timber.e("Recommended keywords fetch failed: ${response.code()}, message: $errorMsg")
                Result.failure(Exception("Failed to fetch recommended keywords: $errorMsg"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch recommended keywords")
            Result.failure(e)
        }
    }
}
