package com.project.seoulmate.data.repository

import com.project.seoulmate.signup.AuthApi
import com.project.seoulmate.signup.LoginRequest
import com.project.seoulmate.signup.MemberResponse
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi // Hilt가 NetworkModule에서 만든 Api를 주입해줍니다.
) : AuthRepository {
    override suspend fun login(token: String, request: LoginRequest): Response<MemberResponse> {
        return authApi.login(token, request)
    }
}