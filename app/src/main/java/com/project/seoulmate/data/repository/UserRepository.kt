package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.ReviewItem
import com.project.seoulmate.data.remote.BadgeResponse
import com.project.seoulmate.data.remote.PageResponse
import com.project.seoulmate.data.remote.UserProfileResponse

/**
 * 사용자 프로필 Repository 인터페이스
 */
interface UserRepository {
    /**
     * 프로필 이미지 업데이트
     * @param imageUrl 업로드된 이미지 URL
     */
    suspend fun updateProfileImage(imageUrl: String): Result<Unit>

    /**
     * 내 프로필 조회
     */
    suspend fun getMyProfile(): Result<UserProfileResponse>

    /**
     * 특정 사용자의 배지 목록 조회
     * @param userId 조회할 사용자 ID
     * @return 배지 목록 (count 내림차순 → lastEarnedAt 내림차순)
     */
    suspend fun getUserBadges(userId: Long): Result<List<BadgeResponse>>

    /**
     * 특정 사용자가 받은 메이트 후기 목록 조회
     * @param memberId 조회할 사용자 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 리뷰 목록 (페이징 처리)
     */
    suspend fun getUserReviews(
        memberId: Long,
        page: Int = 0,
        size: Int = 10
    ): Result<PageResponse<ReviewItem>>

    /**
     * 자기소개 업데이트
     * @param bio 업데이트할 자기소개 내용
     * @return 업데이트된 전체 프로필 정보
     */
    suspend fun updateBio(bio: String): Result<UserProfileResponse>
}
