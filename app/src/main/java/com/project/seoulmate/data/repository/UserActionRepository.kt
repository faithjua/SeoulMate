package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.BlockRequest
import com.project.seoulmate.data.model.ReportRequest

/**
 * 신고/차단 관련 비즈니스 로직을 담당하는 Repository
 */
interface UserActionRepository {
    /**
     * 사용자 또는 게시글 신고
     */
    suspend fun report(
        token: String,
        request: ReportRequest
    ): Result<Unit>

    /**
     * 사용자 차단
     */
    suspend fun block(
        token: String,
        request: BlockRequest
    ): Result<Unit>
}
