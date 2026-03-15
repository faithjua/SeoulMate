package com.project.seoulmate.signup


import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName



@Serializable
data class LoginRequest(
    val email: String,
    val nickname: String,
    val role: String,
    val nationality: String
)

@Serializable
data class MemberResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    //val isNewMember: Boolean? = null,
    //서버가 안주거나 못찾으면 기존유저로 간주하는것이 정말 안전한가?
    @SerialName("newMember")
    val isNewMember: Boolean = false,
    val role: String,
    val nationality: String
)