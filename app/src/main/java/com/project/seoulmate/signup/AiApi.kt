package com.project.seoulmate.signup

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

// 1. AI API 통신 규격서
interface AiApi {
    @POST("/api/courses/ai-generate") // 스프링 부트 주소
    suspend fun generateAiCourse(
        @Body request: AiCourseRequest // 우리가 만든 요청 상자
    ): Response<AiCourseResponse>      // 우리가 받을 응답 상자
}