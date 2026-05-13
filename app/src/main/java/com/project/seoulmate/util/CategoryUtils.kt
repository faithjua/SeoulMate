package com.project.seoulmate.util

import android.content.Context
import com.project.seoulmate.R

/**
 * 카테고리 코드를 기반으로 기기 언어에 맞는 라벨을 가져옵니다.
 *
 * @param code 카테고리 코드 (예: "TOURISM", "KPOP")
 * @param fallbackLabel 매핑이 없을 경우 사용할 기본 라벨 (서버에서 받은 label)
 * @return 기기 언어에 맞는 카테고리 라벨
 */
fun Context.getCategoryLabel(code: String, fallbackLabel: String = ""): String {
    val resId = when (code) {
        "TODAY" -> R.string.category_TODAY
        "TOURISM" -> R.string.category_TOURISM
        "KPOP" -> R.string.category_KPOP
        "KBEAUTY" -> R.string.category_KBEAUTY
        "SHOPPING" -> R.string.category_SHOPPING
        "KOREAN_FOOD" -> R.string.category_KOREAN_FOOD
        "CAFE" -> R.string.category_CAFE
        "TRANSPORT_GUIDE" -> R.string.category_TRANSPORT_GUIDE
        "CLASS" -> R.string.category_CLASS
        "COMMUNITY" -> R.string.category_COMMUNITY
        "EXHIBITION" -> R.string.category_EXHIBITION
        "SAFETY" -> R.string.category_SAFETY
        else -> 0
    }

    return if (resId != 0) getString(resId) else fallbackLabel
}

/**
 * 혼잡도 코드를 기반으로 기기 언어에 맞는 라벨을 가져옵니다.
 *
 * @param code 혼잡도 코드 (예: "RELAXED", "MODERATE", "BUSY")
 * @param fallbackLabel 매핑이 없을 경우 사용할 기본 라벨
 * @return 기기 언어에 맞는 혼잡도 라벨
 */
fun Context.getCongestionLabel(code: String, fallbackLabel: String = ""): String {
    val resId = when (code) {
        "RELAXED" -> R.string.congestion_RELAXED
        "MODERATE" -> R.string.congestion_MODERATE
        "BUSY" -> R.string.congestion_BUSY
        else -> 0
    }

    return if (resId != 0) getString(resId) else fallbackLabel
}
