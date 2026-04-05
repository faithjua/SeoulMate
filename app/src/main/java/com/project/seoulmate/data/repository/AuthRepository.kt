package com.project.seoulmate.data.repository

import com.project.seoulmate.data.remote.ApiResponse
import com.project.seoulmate.signup.SignupRequest
import com.project.seoulmate.signup.AuthResponse
import retrofit2.Response

interface AuthRepository {
    suspend fun login(token: String): Response<ApiResponse<AuthResponse>>
    suspend fun signup(token: String, request: SignupRequest): Response<ApiResponse<AuthResponse>>
}