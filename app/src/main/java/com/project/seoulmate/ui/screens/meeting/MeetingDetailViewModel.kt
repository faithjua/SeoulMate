package com.project.seoulmate.ui.screens.meeting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.project.seoulmate.R
import com.project.seoulmate.data.model.CoursePoint
import com.project.seoulmate.data.model.MateInfo
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.model.MeetingDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MeetingDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val meetingId: String = checkNotNull(savedStateHandle["meetingId"])

    private val _uiState = MutableStateFlow<MeetingDetail?>(null)
    val uiState: StateFlow<MeetingDetail?> = _uiState.asStateFlow()

    init {
        loadMeetingDetail()
    }

    private fun loadMeetingDetail() {
        // UI 구현을 위한 더미 데이터 세팅. 
        // 실제로는 Repository를 통해 데이터를 가져와야 함.
        val dummyMeeting = Meeting(
            id = meetingId,
            title = "창덕궁 탐방 및 맛집 방문",
            time = "3월 23일 오후 6-7시",
            price = "약 20,000원",
            rating = "5.0",
            imageRes = R.drawable.img_recommend_1, // Home과 동일한 더미 이미지 사용
            tags = listOf("#관광", "#맛집", "#커뮤니티")
        )

        val detail = MeetingDetail(
            meeting = dummyMeeting,
            location = "종로구",
            timeElapsed = "10시간 전",
            dateAndTime = "3월 23일 오후 6-7시",
            description = "창덕궁의 숨겨진 명소를 관람한 후 북촌 한옥 마을로 한식 맛집을 함께 가요^^\n\n저녁 비용과 방문 비용 포함해서 전부 2만원만 소요!",
            courses = listOf(
                CoursePoint("창덕궁 (만남 시작)", isStart = true, lat = 37.5824, lng = 126.9917),
                CoursePoint("창경궁 대온실", lat = 37.5816, lng = 126.9961),
                CoursePoint("종묘", lat = 37.5746, lng = 126.9940),
                CoursePoint("북촌 조향사의 집", lat = 37.5815, lng = 126.9840),
                CoursePoint("익선동한옥거리 (만남 종료)", isEnd = true, lat = 37.5745, lng = 126.9890)
            ),
            mateInfo = MateInfo(
                name = "소율이",
                profileRes = R.drawable.img_recommend_1, // 임시 이미지, 없으면 나중에 기본 아이콘으로 교체
                rating = "4.22",
                reviewCount = 83,
                bio = "인스타 @insoul 유튜버\n관광학부 전공으로 재직... [더보기]",
                isVerified = true
            ),
            mateOtherMeetings = listOf(
                Meeting(
                    id = "meeting_other_1",
                    title = "창덕궁 탐방 및 맛집",
                    time = "23일 오후 7~9시",
                    price = "예상 ₩20,000",
                    rating = "5.0",
                    imageRes = R.drawable.img_recommend_1,
                    tags = listOf("관광", "맛집", "커뮤니티")
                ),
                Meeting(
                    id = "meeting_other_2",
                    title = "창덕궁 탐방 및 맛집",
                    time = "28일 오후 7~9시",
                    price = "예상 ₩20,000",
                    rating = "5.0",
                    imageRes = R.drawable.img_recommend_2,
                    tags = listOf("여유", "맛집", "커뮤니티")
                )
            )
        )

        _uiState.value = detail
    }
}
