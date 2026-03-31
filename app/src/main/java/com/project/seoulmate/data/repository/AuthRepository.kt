package com.project.seoulmate.data.repository

import com.project.seoulmate.signup.LoginRequest
import com.project.seoulmate.signup.MemberResponse
import retrofit2.Response

interface AuthRepository {
    suspend fun login(token: String, request: LoginRequest): Response<MemberResponse>
}