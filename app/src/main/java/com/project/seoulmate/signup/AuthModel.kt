package com.project.seoulmate.signup


import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName



@Serializable
data class SignupRequest(
    val email: String,
    val nickname: String,
    val role: String,
    val nationality: String
)

@Serializable
data class AuthResponse(
    // 신규 회원(빈 껍데기)일 때는 이 값들이 서버에서 null로 오기 때문에 모두 ?(Nullable) 처리하고 기본값 = null을 줍니다.
    val id: Long? = null,
    val email: String? = null,
    val nickname: String? = null,
    val role: String? = null,
    val nationality: String? = null,

    // 이 값은 무조건 서버에서 오므로 Nullable이 아니어도 됩니다.
    @SerialName("newMember")
    val isNewMember: Boolean = false
)

