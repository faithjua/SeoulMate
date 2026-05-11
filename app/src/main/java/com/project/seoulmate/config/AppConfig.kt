package com.project.seoulmate.config

/**
 * 앱 전역 설정 오브젝트.
 * IS_PRODUCTION = true 로 설정하면 미구현 기능의 UI 요소가 화면에서 사라집니다.
 * 심사 제출 / 데모 환경에서는 true, 개발 중일 때는 false로 유지하세요.
 */
object AppConfig {
    // true → 프로덕션 모드 (미구현 UI 숨김)
    // false → 개발 모드 (모든 UI 표시)
    const val IS_PRODUCTION = false
}
