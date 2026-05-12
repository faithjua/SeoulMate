package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.CongestionLevelOption
import com.project.seoulmate.data.remote.CatalogApi
import timber.log.Timber
import javax.inject.Inject

/**
 * CatalogRepository의 실제 구현체
 *
 * 앱 실행 시 1회 호출 후 메모리 캐싱을 수행합니다.
 */
class CatalogRepositoryImpl @Inject constructor(
    private val catalogApi: CatalogApi
) : CatalogRepository {

    // 메모리 캐시
    private var cachedCategories: List<String>? = null
    private var cachedCongestionLevels: List<CongestionLevelOption>? = null

    override suspend fun getCategories(): Result<List<String>> {
        // 캐시가 있으면 반환
        cachedCategories?.let {
            Timber.d("Returning cached categories: ${it.size} items")
            return Result.success(it)
        }

        return try {
            val response = catalogApi.getCategories()

            if (response.isSuccessful && response.body() != null) {
                val categories = response.body()!!.data
                cachedCategories = categories
                Timber.d("Categories loaded and cached: ${categories.size} items")
                Result.success(categories)
            } else {
                // API 실패 시 폴백 데이터 사용 (백엔드 배포 전 임시)
                Timber.w("카테고리 API 실패: ${response.code()}, 폴백 데이터 사용")
                val fallbackCategories = listOf(
                    "당일만남", "관광", "K-팝", "K-뷰티", "쇼핑", "한식", "카페",
                    "교통가이드", "클래스", "커뮤니티", "전시·스타일", "안전·생활"
                )
                cachedCategories = fallbackCategories
                Result.success(fallbackCategories)
            }
        } catch (e: Exception) {
            // 네트워크 에러 등 예외 발생 시 폴백 데이터 사용
            Timber.e(e, "Get categories error, 폴백 데이터 사용")
            val fallbackCategories = listOf(
                "당일만남", "관광", "K-팝", "K-뷰티", "쇼핑", "한식", "카페",
                "교통가이드", "클래스", "커뮤니티", "전시·스타일", "안전·생활"
            )
            cachedCategories = fallbackCategories
            Result.success(fallbackCategories)
        }
    }

    override suspend fun getCongestionLevels(): Result<List<CongestionLevelOption>> {
        // 캐시가 있으면 반환
        cachedCongestionLevels?.let {
            Timber.d("Returning cached congestion levels: ${it.size} items")
            return Result.success(it)
        }

        return try {
            val response = catalogApi.getCongestionLevels()

            if (response.isSuccessful && response.body() != null) {
                val levels = response.body()!!.data
                cachedCongestionLevels = levels
                Timber.d("Congestion levels loaded and cached: ${levels.size} items")
                Result.success(levels)
            } else {
                // API 실패 시 폴백 데이터 사용 (백엔드 배포 전 임시)
                Timber.w("혼잡도 API 실패: ${response.code()}, 폴백 데이터 사용")
                val fallbackLevels = listOf(
                    CongestionLevelOption(code = "ALL", label = "전체"),
                    CongestionLevelOption(code = "RELAXED", label = "여유"),
                    CongestionLevelOption(code = "NORMAL", label = "보통"),
                    CongestionLevelOption(code = "SLIGHTLY_BUSY", label = "약간 붐빔"),
                    CongestionLevelOption(code = "BUSY", label = "붐빔")
                )
                cachedCongestionLevels = fallbackLevels
                Result.success(fallbackLevels)
            }
        } catch (e: Exception) {
            // 네트워크 에러 등 예외 발생 시 폴백 데이터 사용
            Timber.e(e, "Get congestion levels error, 폴백 데이터 사용")
            val fallbackLevels = listOf(
                CongestionLevelOption(code = "ALL", label = "전체"),
                CongestionLevelOption(code = "RELAXED", label = "여유"),
                CongestionLevelOption(code = "NORMAL", label = "보통"),
                CongestionLevelOption(code = "SLIGHTLY_BUSY", label = "약간 붐빔"),
                CongestionLevelOption(code = "BUSY", label = "붐빔")
            )
            cachedCongestionLevels = fallbackLevels
            Result.success(fallbackLevels)
        }
    }

    /**
     * 캐시 초기화 (필요 시 사용)
     */
    fun clearCache() {
        cachedCategories = null
        cachedCongestionLevels = null
        Timber.d("Catalog cache cleared")
    }
}
