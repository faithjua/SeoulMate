package com.project.seoulmate.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import kotlinx.serialization.Serializable

/**
 * 이미지 업로드 API 인터페이스
 */
interface ImageApi {

    /**
     * 이미지 업로드 (S3)
     * POST /api/images
     */
    @Multipart
    @POST("/api/images")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("folder") folder: RequestBody
    ): Response<ApiResponse<ImageUploadResponse>>

    /**
     * 다중 이미지 업로드
     * POST /api/images/bulk
     */
    @Multipart
    @POST("/api/images/bulk")
    suspend fun uploadImages(
        @Header("Authorization") token: String,
        @Part files: List<MultipartBody.Part>,
        @Part("folder") folder: RequestBody
    ): Response<ApiResponse<List<ImageUploadResponse>>>
}

/**
 * 이미지 업로드 응답 DTO
 */
@Serializable
data class ImageUploadResponse(
    val url: String
)
