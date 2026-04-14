package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.AiCourseRequest
import com.project.seoulmate.data.model.AiCourseResponse
import com.project.seoulmate.data.model.CourseCreateRequest
import com.project.seoulmate.data.model.CourseDetailResponse
import com.project.seoulmate.data.model.CourseListResponse
import com.project.seoulmate.data.remote.ApiResponse
import com.project.seoulmate.data.remote.PageResponse
import com.project.seoulmate.signup.CourseApi
import retrofit2.Response

import javax.inject.Inject


class CourseRepositoryImpl @Inject constructor(
    private val courseApi: CourseApi
): CourseRepository {
    override suspend fun getCourses(page: Int, size: Int): Response<ApiResponse<PageResponse<CourseListResponse>>> {
        return courseApi.getCourses(page, size)
    }

    override suspend fun getCourseDetail(courseId: Long): Response<ApiResponse<CourseDetailResponse>> {
        return courseApi.getCourseDetail(courseId)
    }

    override suspend fun generateAiCourse(token:String,request: AiCourseRequest): Response<ApiResponse<AiCourseResponse>> {
        return courseApi.generateAiCourse(token,request)
    }

    override suspend fun createCourse(token: String, request: CourseCreateRequest): Response<ApiResponse<CourseDetailResponse>> {
        return courseApi.createCourse(token, request)
    }
}
