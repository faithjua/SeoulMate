package com.project.seoulmate.data.repository

import com.google.android.gms.common.api.Api
import com.project.seoulmate.data.remote.ApiResponse
import com.project.seoulmate.signup.AuthApi
import com.project.seoulmate.signup.SignupRequest
import com.project.seoulmate.signup.AuthResponse
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi // Hilt가 NetworkModule에서 만든 Api를 주입
) : AuthRepository {
    override suspend fun login(token: String): Response<ApiResponse<AuthResponse>> {
        return authApi.login(token)
    }
    override suspend fun signup(token: String, request: SignupRequest): Response<ApiResponse<AuthResponse>> {
        return authApi.signup(token, request)
    }
}