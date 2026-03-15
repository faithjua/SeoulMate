package com.project.seoulmate.signup

//package com.project.seoulmate.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(
        // 스프링 부트 수문장(필터)을 통과하기 위해 헤더에 Firebase 토큰을 넣음
        @Header("Authorization") token: String,
        // 바디(Body)에는 유저가 쓴 이메일, 닉네임, role,국적을 담음
        @Body request: LoginRequest
    ): Response<MemberResponse>
}