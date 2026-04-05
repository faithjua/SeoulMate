package com.project.seoulmate.data.remote

import kotlinx.serialization.Serializable

/**
 * 스프링 부트의 PageResponse<T> 페이징 규격과 완벽하게 일치하는 코틀린 데이터 클래스
 */
@Serializable
data class PageResponse<T>(
    val content: List<T>,       // 실제 데이터 목록 (예: 코스 목록 리스트)
    val page: Int,              // 현재 페이지 번호 (0부터 시작)
    val size: Int,              // 페이지 크기
    val totalElements: Long,    // 전체 데이터 수
    val totalPages: Int,        // 전체 페이지 수
    val first: Boolean,         // 첫 번째 페이지 여부
    val last: Boolean           // 마지막 페이지 여부 (안드로이드 무한 스크롤 구현 시 핵심!)
)