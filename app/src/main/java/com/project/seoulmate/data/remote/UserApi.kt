package com.project.seoulmate.data.remote

import com.project.seoulmate.data.model.ReviewItem
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.*

/**
 * 사용자 프로필 관련 API 인터페이스
 */
interface UserApi {

    /**
     * 프로필 이미지 업데이트
     * PATCH /api/users/me/profile-image
     */
    @PATCH("/api/users/me/profile-image")
    suspend fun updateProfileImage(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileImageRequest
    ): Response<ApiResponse<Unit>>

    /**
     * 내 프로필 조회
     * GET /api/users/me
     */
    @GET("/api/users/me")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<ApiResponse<UserProfileResponse>>

    /**
     * 특정 사용자의 배지 목록 조회
     * GET /api/users/{userId}/badges
     *
     * 비로그인 사용자도 조회 가능
     */
    @GET("/api/users/{userId}/badges")
    suspend fun getUserBadges(
        @Path("userId") userId: Long
    ): Response<ApiResponse<List<BadgeResponse>>>

    /**
     * 특정 사용자가 받은 메이트 후기 목록 조회
     * GET /api/users/{memberId}/reviews
     *
     * @param memberId 조회할 사용자 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 리뷰 목록 (페이징 처리)
     */
    @GET("/api/users/{memberId}/reviews")
    suspend fun getUserReviews(
        @Path("memberId") memberId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PageResponse<ReviewItem>>>

    /**
     * 자기소개 업데이트
     * PATCH /api/users/me/bio
     *
     * @param token Firebase ID 토큰
     * @param request 업데이트할 자기소개 내용
     * @return 업데이트된 전체 프로필 정보
     */
    @PATCH("/api/users/me/bio")
    suspend fun updateBio(
        @Header("Authorization") token: String,
        @Body request: UpdateBioRequest
    ): Response<ApiResponse<UserProfileResponse>>
}

/**
 * 프로필 이미지 업데이트 요청 DTO
 */
@Serializable
data class UpdateProfileImageRequest(
    val profileImageUrl: String
)

/**
 * 자기소개 업데이트 요청 DTO
 */
@Serializable
data class UpdateBioRequest(
    val bio: String
)

/**
 * 사용자 프로필 응답 DTO
 */
@Serializable
data class UserProfileResponse(
    val id: Long,
    val nickname: String,
    val email: String? = null,
    val profileImageUrl: String? = null,
    val bio: String? = null,
    val rating: Double? = null,
    val reviewCount: Int = 0,
    val isVerified: Boolean = false
)

/**
 * 배지 응답 DTO
 *
 * @param categoryCode 카테고리 코드 (한국어, 예: "관광", "K-팝")
 * @param count 누적 획득 횟수
 * @param firstEarnedAt 최초 획득 시각 (ISO 8601 format)
 * @param lastEarnedAt 마지막 획득 시각 (ISO 8601 format)
 */
@Serializable
data class BadgeResponse(
    val categoryCode: String,
    val count: Int,
    val firstEarnedAt: String,
    val lastEarnedAt: String
)
