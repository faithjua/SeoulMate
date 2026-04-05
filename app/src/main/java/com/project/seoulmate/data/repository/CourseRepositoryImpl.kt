package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.AiCourseRequest
import com.project.seoulmate.data.model.AiCourseResponse
import com.project.seoulmate.data.model.CourseCreateRequest
import com.project.seoulmate.data.remote.ApiResponse
import com.project.seoulmate.signup.CourseApi
import retrofit2.Response

import javax.inject.Inject


class CourseRepositoryImpl @Inject constructor(
    private val courseApi: CourseApi
): CourseRepository {
    override suspend fun generateAiCourse(token:String,request: AiCourseRequest): Response<ApiResponse<AiCourseResponse>> {
        return courseApi.generateAiCourse(token,request)
    }

    override suspend fun createCourse(token:String,request: CourseCreateRequest): Response<ApiResponse<Unit>> {
        return courseApi.createCourse(token,request)
    }
}
