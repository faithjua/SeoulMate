package com.project.seoulmate.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.data.remote.ImageApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * ImageRepository 구현체
 */
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageApi: ImageApi,
    private val auth: FirebaseAuth
) : ImageRepository {

    override suspend fun uploadImage(uri: Uri, folder: String): Result<String> {
        return try {
            // URI의 MIME 타입 가져오기
            val rawMimeType = context.contentResolver.getType(uri)
            Timber.d("Raw MIME type from URI: $rawMimeType")

            // MIME 타입 정규화 (image/jpg -> image/jpeg)
            val mimeType = normalizeMimeType(rawMimeType ?: "image/jpeg")
            Timber.d("Normalized MIME type: $mimeType")

            // URI를 File로 변환
            val fileData = uriToFile(uri, mimeType) ?: return Result.failure(Exception("Failed to convert URI to File"))
            uploadImage(fileData.file, fileData.mimeType, folder)
        } catch (e: Exception) {
            Timber.e(e, "Failed to upload image from URI")
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(file: File, folder: String): Result<String> {
        // MIME 타입을 파일 확장자에서 추론
        val rawMimeType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
        val mimeType = normalizeMimeType(rawMimeType)
        Timber.d("File extension: ${file.extension}, MIME type: $mimeType")
        return uploadImage(file, mimeType, folder)
    }

    private suspend fun uploadImage(file: File, mimeType: String, folder: String): Result<String> {
        return try {
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
                ?: return Result.failure(Exception("User not authenticated"))

            Timber.d("=== Image Upload Start ===")
            Timber.d("File name: ${file.name}")
            Timber.d("File extension: ${file.extension}")
            Timber.d("File exists: ${file.exists()}")
            Timber.d("MIME type: $mimeType")
            Timber.d("Size: ${file.length()} bytes")
            Timber.d("Folder: $folder")

            // MIME 타입 검증
            if (mimeType !in listOf("image/jpeg", "image/png", "image/webp")) {
                val error = "Invalid MIME type: $mimeType. Only image/jpeg, image/png, image/webp are allowed"
                Timber.e(error)
                return Result.failure(Exception(error))
            }

            // 파일 확장자가 MIME 타입과 일치하는지 확인
            val expectedExtension = when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            if (file.extension.lowercase() != expectedExtension) {
                Timber.w("File extension mismatch: ${file.extension} != $expectedExtension for MIME $mimeType")
            }

            // File을 MultipartBody.Part로 변환 (정확한 MIME 타입 사용)
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val folderBody = folder.toRequestBody("text/plain".toMediaTypeOrNull())

            Timber.d("Request file Content-Type: ${requestFile.contentType()}")

            Timber.d("Sending request to server...")
            val response = imageApi.uploadImage(
                token = "Bearer $token",
                file = body,
                folder = folderBody
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val imageUrl = response.body()?.data?.url
                    ?: return Result.failure(Exception("Image URL is null"))
                Timber.d("✓ Image uploaded successfully: $imageUrl")
                Timber.d("=== Image Upload End ===")
                Result.success(imageUrl)
            } else {
                val errorMsg = response.body()?.message ?: "Unknown error"
                Timber.e("✗ Image upload failed: ${response.code()}, message: $errorMsg")
                Timber.d("=== Image Upload End ===")
                Result.failure(Exception("이미지 업로드 실패: $errorMsg"))
            }
        } catch (e: Exception) {
            Timber.e(e, "✗ Failed to upload image")
            Timber.d("=== Image Upload End ===")
            Result.failure(e)
        }
    }

    override suspend fun uploadImages(uris: List<Uri>, folder: String): Result<List<String>> {
        return try {
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
                ?: return Result.failure(Exception("User not authenticated"))

            // URIs를 Files로 변환
            val fileDataList = uris.mapNotNull { uri ->
                val rawMimeType = context.contentResolver.getType(uri)
                val mimeType = normalizeMimeType(rawMimeType ?: "image/jpeg")
                Timber.d("URI: $uri, Raw MIME: $rawMimeType, Normalized: $mimeType")
                uriToFile(uri, mimeType)
            }
            if (fileDataList.isEmpty()) {
                return Result.failure(Exception("No valid files to upload"))
            }

            // Files를 MultipartBody.Part 리스트로 변환 (정확한 MIME 타입 사용)
            val parts = fileDataList.mapIndexed { index, fileData ->
                Timber.d("=== Preparing file ${index + 1}/${fileDataList.size} ===")
                Timber.d("  Name: ${fileData.file.name}")
                Timber.d("  Extension: ${fileData.file.extension}")
                Timber.d("  MIME: ${fileData.mimeType}")
                Timber.d("  Size: ${fileData.file.length()} bytes")

                val requestFile = fileData.file.asRequestBody(fileData.mimeType.toMediaTypeOrNull())
                Timber.d("  Content-Type: ${requestFile.contentType()}")

                MultipartBody.Part.createFormData("files", fileData.file.name, requestFile)
            }
            val folderBody = folder.toRequestBody("text/plain".toMediaTypeOrNull())

            Timber.d("Uploading ${parts.size} images to folder: $folder")
            val response = imageApi.uploadImages(
                token = "Bearer $token",
                files = parts,
                folder = folderBody
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val imageUrls = response.body()?.data
                    ?: return Result.failure(Exception("Image URLs are null"))
                Timber.d("✓ Images uploaded successfully: ${imageUrls.size} images")
                imageUrls.forEachIndexed { index, url ->
                    Timber.d("  Image ${index + 1}: $url")
                }
                Result.success(imageUrls)
            } else {
                val errorMsg = response.body()?.message ?: "Unknown error"
                Timber.e("✗ Images upload failed: ${response.code()}, message: $errorMsg")
                Result.failure(Exception("이미지 업로드 실패: $errorMsg"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to upload images")
            Result.failure(e)
        }
    }

    /**
     * URI를 File로 변환
     * @param uri 이미지 URI
     * @param mimeType 정규화된 MIME 타입 (예: "image/jpeg", "image/png", "image/webp")
     * @return FileData (파일과 MIME 타입)
     */
    private fun uriToFile(uri: Uri, mimeType: String): FileData? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // MIME 타입에 따라 파일 확장자 결정 (이미 정규화되어 있음)
            val extension = when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> {
                    Timber.w("Unexpected MIME type in uriToFile: $mimeType, using jpg")
                    "jpg"
                }
            }

            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.$extension")
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Timber.d("File created: ${file.name}, size: ${file.length()} bytes, MIME: $mimeType")
            FileData(file, mimeType)
        } catch (e: Exception) {
            Timber.e(e, "Failed to convert URI to File")
            null
        }
    }

    override suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
                ?: return Result.failure(Exception("User not authenticated"))

            Timber.d("Deleting image: $imageUrl")

            val response = imageApi.deleteImage(
                token = "Bearer $token",
                url = imageUrl
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Timber.d("Image deleted successfully: $imageUrl")
                Result.success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: "Unknown error"
                Timber.e("Image delete failed: ${response.code()}, message: $errorMsg")
                Result.failure(Exception("Image delete failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete image")
            Result.failure(e)
        }
    }

    /**
     * MIME 타입 정규화
     * Android에서 반환하는 비표준 MIME 타입을 표준 형식으로 변환
     * 백엔드는 image/jpeg, image/png, image/webp만 허용
     *
     * @param mimeType 원본 MIME 타입
     * @return 정규화된 MIME 타입
     */
    private fun normalizeMimeType(mimeType: String): String {
        return when (mimeType.lowercase()) {
            "image/jpg", "image/jpeg" -> "image/jpeg"
            "image/png" -> "image/png"
            "image/webp" -> "image/webp"
            // 비표준 형식 처리
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> {
                Timber.w("Unknown MIME type: $mimeType, defaulting to image/jpeg")
                "image/jpeg"
            }
        }
    }

    /**
     * 파일과 MIME 타입을 함께 저장하는 데이터 클래스
     */
    private data class FileData(
        val file: File,
        val mimeType: String
    )
}
