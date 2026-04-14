package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.AiCourseRequest
import com.project.seoulmate.data.model.AiCourseResponse
import com.project.seoulmate.data.model.CourseCreateRequest
import com.project.seoulmate.data.model.CourseDetailResponse
import com.project.seoulmate.data.model.CourseListResponse
import com.project.seoulmate.data.remote.ApiResponse
import com.project.seoulmate.data.remote.PageResponse
import retrofit2.Response

interface CourseRepository {
    // 0. 코스 목록 조회
    suspend fun getCourses(page: Int = 0, size: Int = 10): Response<ApiResponse<PageResponse<CourseListResponse>>>

    // 0-1. 코스 상세 조회
    suspend fun getCourseDetail(courseId: Long): Response<ApiResponse<CourseDetailResponse>>

    // 1. 일반: 사용자가 직접 장소를 선택해서 코스 생성
    suspend fun createCourse(token: String, request: CourseCreateRequest): Response<ApiResponse<CourseDetailResponse>>

    // 2. AI: 프롬프트를 기반으로 AI 코스 추천받기
    suspend fun generateAiCourse(token:String, request: AiCourseRequest): Response<ApiResponse<AiCourseResponse>>
}