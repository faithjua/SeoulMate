package com.project.seoulmate.data.remote

import kotlinx.serialization.Serializable
/**
 * 스프링 부트의 ApiResponse<T> 규격과 완벽하게 일치하는 코틀린 데이터 클래스
 */
@Serializable
data class ApiResponse<T>(
    // 백엔드의 boolean success와 매핑
    val success: Boolean,

    // 백엔드의 String message와 매핑 (null이 올 수도 있으므로 ? 처리)
    val message: String? = null,

    // 백엔드의 T data와 매핑 (데이터가 없는 ok() 응답 등에서는 null이 됨)
    val data: T? = null
)