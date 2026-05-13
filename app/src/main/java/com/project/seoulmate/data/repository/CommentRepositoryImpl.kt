package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.CommentCreateRequest
import com.project.seoulmate.data.model.CommentResponse
import com.project.seoulmate.data.model.CommentUpdateRequest
import com.project.seoulmate.data.remote.CommentApi
import com.project.seoulmate.data.remote.PageResponse
import timber.log.Timber
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val commentApi: CommentApi
) : CommentRepository {

    override suspend fun getComments(
        token: String?,
        meetupId: Long,
        page: Int,
        size: Int
    ): Result<PageResponse<CommentResponse>> = runCatching {
        val authHeader = token?.let { "Bearer $it" }
        val response = commentApi.getComments(authHeader, meetupId, page, size)
        if (response.isSuccessful && response.body()?.success == true) {
            response.body()?.data
                ?: throw IllegalStateException("댓글 데이터가 비어 있습니다")
        } else {
            val msg = response.body()?.message ?: "댓글 조회 실패 (${response.code()})"
            Timber.e("getComments failed: $msg")
            throw IllegalStateException(msg)
        }
    }

    override suspend fun createComment(
        token: String,
        meetupId: Long,
        content: String,
        isPrivate: Boolean
    ): Result<CommentResponse> = runCatching {
        val response = commentApi.createComment(
            token = "Bearer $token",
            meetupId = meetupId,
            request = CommentCreateRequest(content = content, isPrivate = isPrivate)
        )
        if (response.isSuccessful && response.body()?.success == true) {
            response.body()?.data
                ?: throw IllegalStateException("댓글 작성 응답이 비어 있습니다")
        } else {
            val msg = response.body()?.message ?: "댓글 작성 실패 (${response.code()})"
            Timber.e("createComment failed: $msg")
            throw IllegalStateException(msg)
        }
    }

    override suspend fun updateComment(
        token: String,
        meetupId: Long,
        commentId: Long,
        content: String
    ): Result<CommentResponse> = runCatching {
        val response = commentApi.updateComment(
            token = "Bearer $token",
            meetupId = meetupId,
            commentId = commentId,
            request = CommentUpdateRequest(content = content)
        )
        if (response.isSuccessful && response.body()?.success == true) {
            response.body()?.data
                ?: throw IllegalStateException("댓글 수정 응답이 비어 있습니다")
        } else {
            val msg = response.body()?.message ?: "댓글 수정 실패 (${response.code()})"
            Timber.e("updateComment failed: $msg")
            throw IllegalStateException(msg)
        }
    }

    override suspend fun deleteComment(
        token: String,
        meetupId: Long,
        commentId: Long
    ): Result<Unit> = runCatching {
        val response = commentApi.deleteComment(
            token = "Bearer $token",
            meetupId = meetupId,
            commentId = commentId
        )
        if (!(response.isSuccessful && response.body()?.success == true)) {
            val msg = response.body()?.message ?: "댓글 삭제 실패 (${response.code()})"
            Timber.e("deleteComment failed: $msg")
            throw IllegalStateException(msg)
        }
    }
}