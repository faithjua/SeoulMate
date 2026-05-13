package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.CategoryItem
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
    private var cachedCategories: List<CategoryItem>? = null
    private var cachedCongestionLevels: List<CongestionLevelOption>? = null

    override suspend fun getCategories(): Result<List<CategoryItem>> {
        // 캐시가 있으면 반환
        cachedCategories?.let {
            Timber.d("Returning cached categories: ${it.size} items")
            return Result.success(it)
        }

        return try {
            val response = catalogApi.getCategories()

            if (response.isSuccessful && response.body()?.success == true) {
                val categories = response.body()?.data ?: emptyList()
                cachedCategories = categories
                Timber.d("Categories loaded and cached: ${categories.size} items")
                Result.success(categories)
            } else {
                // API 실패 시 폴백 데이터 사용 (백엔드 배포 전 임시)
                val errorMsg = response.body()?.message ?: "Unknown error"
                Timber.w("카테고리 API 실패: ${response.code()}, message: $errorMsg, 폴백 데이터 사용")
                val fallbackCategories = listOf(
                    CategoryItem(code = "TODAY", label = "당일만남"),
                    CategoryItem(code = "TOURISM", label = "관광"),
                    CategoryItem(code = "KPOP", label = "K-팝"),
                    CategoryItem(code = "KBEAUTY", label = "K-뷰티"),
                    CategoryItem(code = "SHOPPING", label = "쇼핑"),
                    CategoryItem(code = "KOREAN_FOOD", label = "한식"),
                    CategoryItem(code = "CAFE", label = "카페"),
                    CategoryItem(code = "TRANSPORT_GUIDE", label = "교통가이드"),
                    CategoryItem(code = "CLASS", label = "클래스"),
                    CategoryItem(code = "COMMUNITY", label = "커뮤니티"),
                    CategoryItem(code = "EXHIBITION", label = "전시·스타일"),
                    CategoryItem(code = "SAFETY", label = "안전·생활")
                )
                cachedCategories = fallbackCategories
                Result.success(fallbackCategories)
            }
        } catch (e: Exception) {
            // 네트워크 에러 등 예외 발생 시 폴백 데이터 사용
            Timber.e(e, "Get categories error, 폴백 데이터 사용")
            val fallbackCategories = listOf(
                CategoryItem(code = "TODAY", label = "당일만남"),
                CategoryItem(code = "TOURISM", label = "관광"),
                CategoryItem(code = "KPOP", label = "K-팝"),
                CategoryItem(code = "KBEAUTY", label = "K-뷰티"),
                CategoryItem(code = "SHOPPING", label = "쇼핑"),
                CategoryItem(code = "KOREAN_FOOD", label = "한식"),
                CategoryItem(code = "CAFE", label = "카페"),
                CategoryItem(code = "TRANSPORT_GUIDE", label = "교통가이드"),
                CategoryItem(code = "CLASS", label = "클래스"),
                CategoryItem(code = "COMMUNITY", label = "커뮤니티"),
                CategoryItem(code = "EXHIBITION", label = "전시·스타일"),
                CategoryItem(code = "SAFETY", label = "안전·생활")
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

            if (response.isSuccessful && response.body()?.success == true) {
                val levels = response.body()?.data ?: emptyList()
                cachedCongestionLevels = levels
                Timber.d("Congestion levels loaded and cached: ${levels.size} items")
                Result.success(levels)
            } else {
                // API 실패 시 폴백 데이터 사용 (백엔드 배포 전 임시)
                val errorMsg = response.body()?.message ?: "Unknown error"
                Timber.w("혼잡도 API 실패: ${response.code()}, message: $errorMsg, 폴백 데이터 사용")
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
