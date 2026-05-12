package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.CommentResponse
import com.project.seoulmate.data.remote.PageResponse

interface CommentRepository {

    suspend fun getComments(
        token: String?,
        meetupId: Long,
        page: Int = 0,
        size: Int = 20
    ): Result<PageResponse<CommentResponse>>

    suspend fun createComment(
        token: String,
        meetupId: Long,
        content: String,
        isPrivate: Boolean
    ): Result<CommentResponse>

    /** 서버는 content만 받는다 */
    suspend fun updateComment(
        token: String,
        meetupId: Long,
        commentId: Long,
        content: String
    ): Result<CommentResponse>

    suspend fun deleteComment(
        token: String,
        meetupId: Long,
        commentId: Long
    ): Result<Unit>
}