package com.project.seoulmate.data.model

import kotlinx.serialization.Serializable

/**
 * 네이버 Local Search API 응답
 */
@Serializable
data class NaverSearchResponse(
    val lastBuildDate: String,
    val total: Int,
    val start: Int,
    val display: Int,
    val items: List<NaverSearchItem>
)

/**
 * 네이버 검색 결과 개별 항목
 */
@Serializable
data class NaverSearchItem(
    val title: String,           // 장소명 (HTML 태그 포함 가능: <b>강남</b>역)
    val link: String = "",       // 네이버 지도 링크
    val category: String = "",   // 카테고리 (예: 음식점>한식>육류,고기요리)
    val description: String = "",
    val telephone: String = "",
    val address: String,         // 지번 주소
    val roadAddress: String,     // 도로명 주소
    val mapx: String,            // X 좌표 (경도) - 네이버 좌표계 (카텍 좌표 * 10^7)
    val mapy: String             // Y 좌표 (위도) - 네이버 좌표계 (카텍 좌표 * 10^7)
) {
    /**
     * HTML 태그 제거한 제목
     */
    fun getCleanTitle(): String {
        return title.replace("<b>", "").replace("</b>", "")
    }

    /**
     * 네이버 좌표를 일반 좌표로 변환 (위도)
     */
    fun getLatitude(): Double {
        return mapy.toDoubleOrNull()?.div(10_000_000.0) ?: 0.0
    }

    /**
     * 네이버 좌표를 일반 좌표로 변환 (경도)
     */
    fun getLongitude(): Double {
        return mapx.toDoubleOrNull()?.div(10_000_000.0) ?: 0.0
    }

    /**
     * 도로명 주소 우선, 없으면 지번 주소
     */
    fun getBestAddress(): String {
        return roadAddress.ifEmpty { address }
    }
}
