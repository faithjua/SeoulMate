package com.project.seoulmate.data.model

import androidx.annotation.DrawableRes

/**
 * 만남 공고 상세 페이지에 필요한 데이터 모델
 */
data class MeetingDetail(
    val meeting: Meeting,
    val location: String,
    val timeElapsed: String,
    val dateAndTime: String,
    val description: String,
    val courses: List<CoursePoint>,
    val mateInfo: MateInfo,
    val mateOtherMeetings: List<Meeting>
)

data class CoursePoint(
    val name: String,
    val isStart: Boolean = false,
    val isEnd: Boolean = false,
    val lat: Double,
    val lng: Double
)

data class MateInfo(
    val name: String,
    @DrawableRes val profileRes: Int,
    val rating: String,
    val reviewCount: Int,
    val bio: String,
    val isVerified: Boolean = false
)
