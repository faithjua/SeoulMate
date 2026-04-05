package com.project.seoulmate.signup

import com.project.seoulmate.data.model.AiCourseRequest
import com.project.seoulmate.data.model.AiCourseResponse
import com.project.seoulmate.data.model.CourseCreateRequest
import com.project.seoulmate.data.remote.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.Header

// 1. AI API 통신 규격서
interface CourseApi {
    @POST("/api/courses/ai-generate") // 스프링 부트 주소
    suspend fun generateAiCourse(
        @Header("Authorization") token: String, //  토큰 파라미터 추가!
        @Body request: AiCourseRequest // 우리가 만든 요청 상자
    ): Response<ApiResponse<AiCourseResponse>>      // 우리가 받을 응답 상자

    // 2. 최종 코스 DB에 저장하기 (추가됨!)
    @POST("/api/courses")
    suspend fun createCourse(
        @Header("Authorization") token: String, // 토큰 파라미터
        @Body request: CourseCreateRequest): Response<ApiResponse<Unit>>

}