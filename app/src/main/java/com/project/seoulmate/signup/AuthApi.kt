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
        @Header("Authorization") token: String
    ): Response<MemberResponse>

    // 회원가입: 추가 정보를 바디에 담아서 보냄
    @POST("/api/auth/signup")
    suspend fun signup(
        @Header("Authorization") token: String,
        @Body request: SignupRequest
    ): Response<MemberResponse>
}