package com.project.seoulmate.data.remote

import com.project.seoulmate.data.model.CategoryItem
import com.project.seoulmate.data.model.CongestionLevelOption
import retrofit2.Response
import retrofit2.http.GET

/**
 * 카탈로그 API 인터페이스
 *
 * 필터 드롭다운 옵션 목록을 반환합니다.
 * 앱 실행 시 1회 호출 후 로컬 캐싱 권장 (정적 데이터)
 */
interface CatalogApi {

    /**
     * 카테고리 12종 조회
     * GET /api/catalog/categories
     *
     * @return ApiResponse<List<CategoryItem>>
     */
    @GET("/api/catalog/categories")
    suspend fun getCategories(): Response<ApiResponse<List<CategoryItem>>>

    /**
     * 혼잡도 옵션 조회 (전체 포함 5종)
     * GET /api/catalog/congestion-levels
     *
     * @return ApiResponse<List<CongestionLevelOption>>
     */
    @GET("/api/catalog/congestion-levels")
    suspend fun getCongestionLevels(): Response<ApiResponse<List<CongestionLevelOption>>>
}
