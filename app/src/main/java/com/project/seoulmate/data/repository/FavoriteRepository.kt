package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.model.favorite.FavoriteItemResponse
import com.project.seoulmate.data.remote.PageResponse

/**
 * 찜(Favorite) 관련 Repository 인터페이스
 */
interface FavoriteRepository {

    /**
     * 찜 추가
     */
    suspend fun addFavorite(token: String, targetType: String, targetId: Long): Result<Unit>

    /**
     * 찜 취소
     */
    suspend fun removeFavorite(token: String, targetType: String, targetId: Long): Result<Unit>

    /**
     * 찜 목록 조회 (페이징)
     */
    suspend fun getFavorites(
        token: String,
        targetType: String? = null,
        page: Int = 0,
        size: Int = 10,
        category: String? = null,
        congestion: String? = null
    ): Result<PageResponse<FavoriteItemResponse>>
}
