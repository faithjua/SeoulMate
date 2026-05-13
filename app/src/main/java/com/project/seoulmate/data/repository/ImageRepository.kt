package com.project.seoulmate.data.repository

import android.net.Uri
import java.io.File

/**
 * 이미지 업로드 Repository 인터페이스
 */
interface ImageRepository {
    /**
     * 단일 이미지 업로드
     * @param uri 이미지 URI
     * @param folder 업로드할 폴더 (예: "profiles", "meetings")
     * @return 업로드된 이미지 URL
     */
    suspend fun uploadImage(uri: Uri, folder: String): Result<String>

    /**
     * 단일 이미지 업로드 (File)
     * @param file 이미지 파일
     * @param folder 업로드할 폴더
     * @return 업로드된 이미지 URL
     */
    suspend fun uploadImage(file: File, folder: String): Result<String>

    /**
     * 다중 이미지 업로드
     * @param uris 이미지 URI 리스트
     * @param folder 업로드할 폴더
     * @return 업로드된 이미지 URL 리스트
     */
    suspend fun uploadImages(uris: List<Uri>, folder: String): Result<List<String>>

    /**
     * 이미지 삭제
     * @param imageUrl 삭제할 이미지의 전체 URL
     * @return 성공 여부 (멱등성 보장 - 이미지가 없어도 성공)
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit>
}
