package com.project.seoulmate.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.data.model.ReviewItem
import com.project.seoulmate.data.remote.BadgeResponse
import com.project.seoulmate.data.remote.PageResponse
import com.project.seoulmate.data.remote.UpdateBioRequest
import com.project.seoulmate.data.remote.UpdateProfileImageRequest
import com.project.seoulmate.data.remote.UserApi
import com.project.seoulmate.data.remote.UserProfileResponse
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * UserRepository 구현체
 */
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val auth: FirebaseAuth
) : UserRepository {

    override suspend fun updateProfileImage(imageUrl: String): Result<Unit> {
        return try {
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
                ?: return Result.failure(Exception("User not authenticated"))

            val response = userApi.updateProfileImage(
                token = "Bearer $token",
                request = UpdateProfileImageRequest(profileImageUrl = imageUrl)
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Timber.d("Profile image updated successfully")
                Result.success(Unit)
            } else {
                Timber.e("Profile image update failed: ${response.code()} ${response.message()}")
                Result.failure(Exception("Profile image update failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to update profile image")
            Result.failure(e)
        }
    }

    override suspend fun getMyProfile(): Result<UserProfileResponse> {
        return try {
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
                ?: return Result.failure(Exception("User not authenticated"))

            val response = userApi.getMyProfile(token = "Bearer $token")

            if (response.isSuccessful && response.body()?.success == true) {
                val profile = response.body()?.data
                    ?: return Result.failure(Exception("Profile data is null"))
                Timber.d("Profile fetched successfully")
                Result.success(profile)
            } else {
                Timber.e("Profile fetch failed: ${response.code()} ${response.message()}")
                Result.failure(Exception("Profile fetch failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch profile")
            Result.failure(e)
        }
    }

    override suspend fun getUserBadges(userId: Long): Result<List<BadgeResponse>> {
        return try {
            Timber.d("Fetching badges for user: $userId")
            val response = userApi.getUserBadges(userId = userId)

            if (response.isSuccessful && response.body()?.success == true) {
                val badges = response.body()?.data ?: emptyList()
                Timber.d("Badges fetched successfully: ${badges.size} badges")
                Result.success(badges)
            } else {
                val errorMsg = response.body()?.message ?: "Unknown error"
                Timber.e("Badge fetch failed: ${response.code()}, message: $errorMsg")
                Result.failure(Exception("Badge fetch failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch badges")
            Result.failure(e)
        }
    }

    override suspend fun getUserReviews(
        memberId: Long,
        page: Int,
        size: Int
    ): Result<PageResponse<ReviewItem>> {
        return try {
            Timber.d("Fetching reviews for user: $memberId, page: $page, size: $size")
            val response = userApi.getUserReviews(
                memberId = memberId,
                page = page,
                size = size
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val pageResponse = response.body()?.data
                    ?: return Result.failure(Exception("Review data is null"))
                Timber.d("Reviews fetched successfully: ${pageResponse.content.size} reviews, total: ${pageResponse.totalElements}")
                Result.success(pageResponse)
            } else {
                val errorMsg = response.body()?.message ?: "Unknown error"
                Timber.e("Review fetch failed: ${response.code()}, message: $errorMsg")
                Result.failure(Exception("Review fetch failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch reviews")
            Result.failure(e)
        }
    }

    override suspend fun updateBio(bio: String): Result<UserProfileResponse> {
        return try {
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
                ?: return Result.failure(Exception("User not authenticated"))

            Timber.d("Updating bio: $bio")
            val response = userApi.updateBio(
                token = "Bearer $token",
                request = UpdateBioRequest(bio = bio)
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val profile = response.body()?.data
                    ?: return Result.failure(Exception("Profile data is null"))
                Timber.d("Bio updated successfully")
                Result.success(profile)
            } else {
                val errorMsg = response.body()?.message ?: "Unknown error"
                Timber.e("Bio update failed: ${response.code()}, message: $errorMsg")
                Result.failure(Exception("Bio update failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to update bio")
            Result.failure(e)
        }
    }
}
