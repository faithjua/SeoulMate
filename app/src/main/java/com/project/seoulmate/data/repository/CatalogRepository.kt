package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.CategoryItem
import com.project.seoulmate.data.model.CongestionLevelOption

/**
 * 카탈로그 Repository 인터페이스
 *
 * 필터 드롭다운 옵션 데이터를 제공합니다.
 */
interface CatalogRepository {

    /**
     * 카테고리 12종 조회
     *
     * @return 카테고리 항목 목록 (code와 label 포함)
     */
    suspend fun getCategories(): Result<List<CategoryItem>>

    /**
     * 혼잡도 옵션 조회 (전체 포함 5종)
     *
     * @return 혼잡도 옵션 목록
     */
    suspend fun getCongestionLevels(): Result<List<CongestionLevelOption>>
}
