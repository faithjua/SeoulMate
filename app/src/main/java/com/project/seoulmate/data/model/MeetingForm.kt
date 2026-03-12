package com.project.seoulmate.data.model

/**
 * 만남 등록 폼의 전체 상태를 나타내는 데이터 클래스.
 */
data class MeetingForm(
    /** 만남 이름 */
    val name: String = "",
    /** 선택된 카테고리/태그 목록 */
    val selectedCategories: Set<String> = emptySet(),
    /** 코스 목록 (장소 이름들) */
    val courses: List<String> = emptyList(),
    /** 요일/시간 슬롯 목록 */
    val timeSlots: List<String> = emptyList(),
    /** 만남 소개 텍스트 */
    val description: String = "",
    /** 예상 지출 금액 */
    val expectedCost: String = "",
    /** 최소 모집 인원 */
    val minMembers: String = "",
    /** 최대 모집 인원 */
    val maxMembers: String = "",
    /** 반복 등록 여부 (null = 미선택, true = 예, false = 아니오) */
    val isRepeating: Boolean? = null,
    /** 업로드된 사진 수 */
    val photoCount: Int = 0
)
