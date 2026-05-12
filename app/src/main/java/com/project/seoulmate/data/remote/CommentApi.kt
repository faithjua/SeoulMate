package com.project.seoulmate.data.remote

import com.project.seoulmate.data.model.CommentCreateRequest
import com.project.seoulmate.data.model.CommentResponse
import com.project.seoulmate.data.model.CommentUpdateRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * 만남 댓글 API (서버 B-12)
 * - 정렬: 오래된 순 (ASC), 서버가 처리
 * - 페이지 기본 20, 최대 50
 * - 본문 최대 500자
 */
interface CommentApi {

    @GET("/api/meetups/{meetupId}/comments")
    suspend fun getComments(
        @Header("Authorization") token: String? = null,
        @Path("meetupId") meetupId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<CommentResponse>>>

    @POST("/api/meetups/{meetupId}/comments")
    suspend fun createComment(
        @Header("Authorization") token: String,
        @Path("meetupId") meetupId: Long,
        @Body request: CommentCreateRequest
    ): Response<ApiResponse<CommentResponse>>

    @PATCH("/api/meetups/{meetupId}/comments/{commentId}")
    suspend fun updateComment(
        @Header("Authorization") token: String,
        @Path("meetupId") meetupId: Long,
        @Path("commentId") commentId: Long,
        @Body request: CommentUpdateRequest
    ): Response<ApiResponse<CommentResponse>>

    @DELETE("/api/meetups/{meetupId}/comments/{commentId}")
    suspend fun deleteComment(
        @Header("Authorization") token: String,
        @Path("meetupId") meetupId: Long,
        @Path("commentId") commentId: Long
    ): Response<ApiResponse<Unit>>
}