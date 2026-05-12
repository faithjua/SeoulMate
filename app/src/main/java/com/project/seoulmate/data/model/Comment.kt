package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

/**
 * 댓글 작성 요청 (POST)
 * 서버: CommentCreateRequest { content, isPrivate? }
 */
@Serializable
data class CommentCreateRequest(
    val content: String,
    val isPrivate: Boolean = false
)

/**
 * 댓글 수정 요청 (PATCH)
 * 서버 CommentUpdateRequest는 content만 받는다. (isPrivate 변경 불가)
 */
@Serializable
data class CommentUpdateRequest(
    val content: String
)

/**
 * 댓글 응답 (스프링부트 CommentResponse 매핑)
 *
 * 서버는 viewer 기준 권한(canEdit/canDelete)을 미리 계산해 내려준다.
 * 클라이언트는 그 플래그만 보고 버튼을 노출하면 된다.
 */
@Serializable
data class CommentResponse(
    val id: Long,
    val content: String,                         // 삭제된 경우 "삭제된 댓글입니다"
    val authorId: Long,
    val authorNickname: String,
    val authorProfileImage: String? = null,
    val isHost: Boolean = false,                 // 호스트가 작성한 댓글인가
    val mateOrder: Int? = null,                  // 만남 멤버 가입 순번 (호스트/비참여자 null)
    val isPrivate: Boolean = false,
    val isDeleted: Boolean = false,
    val canEdit: Boolean = false,                // 현재 viewer가 수정 가능한가
    val canDelete: Boolean = false,              // 현재 viewer가 삭제 가능한가
    val createdAt: String,                       // ISO LocalDateTime
    val updatedAt: String? = null
)