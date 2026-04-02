package com.project.seoulmate.data.repository

import com.project.seoulmate.signup.SignupRequest
import com.project.seoulmate.signup.MemberResponse
import retrofit2.Response

interface AuthRepository {
    suspend fun login(token: String): Response<MemberResponse>
    suspend fun signup(token: String, request: SignupRequest): Response<MemberResponse>
}