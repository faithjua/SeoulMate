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
     *
     * @return 문자열 배열 (URL 리스트)
     */
    @Multipart
    @POST("/api/images/bulk")
    suspend fun uploadImages(
        @Header("Authorization") token: String,
        @Part files: List<MultipartBody.Part>,
        @Part("folder") folder: RequestBody
    ): Response<ApiResponse<List<String>>>

    /**
     * 이미지 삭제 (S3)
     * DELETE /api/images?url={url}
     *
     * @param token Bearer token
     * @param url 삭제할 이미지의 전체 URL
     * @return 멱등성 보장 (객체 없어도 정상 응답)
     */
    @DELETE("/api/images")
    suspend fun deleteImage(
        @Header("Authorization") token: String,
        @Query("url") url: String
    ): Response<ApiResponse<Unit>>
}

/**
 * 이미지 업로드 응답 DTO
 */
@Serializable
data class ImageUploadResponse(
    val url: String
)
