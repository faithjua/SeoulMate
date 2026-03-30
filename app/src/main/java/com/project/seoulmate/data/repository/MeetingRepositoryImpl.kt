package com.project.seoulmate.data.repository

import com.project.seoulmate.R
import com.project.seoulmate.data.model.Category
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.model.MeetingForm
import javax.inject.Inject

/**
 * MeetingRepository의 실제 구현체.
 *
 * 현재는 더미 데이터를 반환
 *
 * @Inject constructor — Hilt가 이 클래스를 자동으로 생성할 수 있게 함
 */
class MeetingRepositoryImpl @Inject constructor() : MeetingRepository {

     // 카테고리 목록 반환
    override fun getCategories(): List<Category> = listOf(
        Category(id = "all", name = "전체메뉴", isAllMenu = true),
         //Category(id = "lightning", name = "번개", iconRes = R.drawable.ic_lightning),
        Category(id = "tourism", name = "관광", iconRes = R.drawable.ic_tourism),
        Category(id = "kpop", name = "K-팝", iconRes = R.drawable.ic_kpop),
        Category(id = "kbeauty", name = "K-뷰티", iconRes = R.drawable.ic_kbeauty),
        Category(id = "shopping", name = "쇼핑", iconRes = R.drawable.ic_shopping),
        Category(id = "food", name = "한식", iconRes = R.drawable.ic_kfood),
        Category(id = "cafe", name = "카페", iconRes = R.drawable.ic_cafe),
        Category(id = "transport", name = "교통 가이드", iconRes = R.drawable.ic_subway),
        Category(id = "accommodation", name = "숙소/지역", iconRes = R.drawable.ic_accommodation),
        Category(id = "class", name = "클래스", iconRes = R.drawable.ic_class),
        Category(id = "community", name = "커뮤니티", iconRes = R.drawable.ic_community),
        Category(id = "exhibition", name = "전시/공연", iconRes = R.drawable.ic_exhibition),
        Category(id = "safety", name = "안전/생활", iconRes = R.drawable.ic_safety)
    )

    /**
     * 최근 본 만남 목록 반환.
     */
    override fun getRecentMeetings(): List<Meeting> = listOf(
        Meeting(
            id = "meeting_1",
            title = "창덕궁 탐방 및 맛집",
            time = "3월 23일 오후 4-5시",
            price = "₩20,000",
            rating = "5.0",
            imageRes = R.drawable.img_recommend_1,
            tags = listOf("혼잡", "#관광", "#한식")
        ),
        Meeting(
            id = "meeting_2",
            title = "창덕궁 탐방 및 맛집",
            time = "4월 1일 오후 4-5시",
            price = "₩20,000",
            rating = "4.8",
            imageRes = R.drawable.img_recommend_2,
            tags = listOf("여유", "#관광", "#한식")
        )
    )

    override suspend fun saveMeetingDraft(form: MeetingForm) {
        // TODO: 서버 API 또는 Room DB로 임시저장 구현
        println("Draft saved: ${form.name}")
    }

    override suspend fun registerMeeting(form: MeetingForm) {
        // TODO: 서버 API로 등록 요청 구현
        println("Meeting registered: ${form.name}")
    }
}
