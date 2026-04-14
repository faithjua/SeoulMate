package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.Category
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.model.MeetingForm
import com.project.seoulmate.data.model.MeetingDetail
import com.project.seoulmate.data.remote.ApiResponse
import com.project.seoulmate.data.remote.PageResponse
import retrofit2.Response

/**
 * MeetingRepository 인터페이스.
 */
interface MeetingRepository {
    /** 카테고리 목록 반환 */
    fun getCategories(): List<Category>

    /** 홈 화면 데이터(최근 만남) 조회 */
    suspend fun getHomeData(category: String? = null): Result<List<Meeting>>

    /** 최근 본 만남 목록 반환 (로컬 기록/더미) - 기존 함수 유지 */
    fun getRecentMeetings(): List<Meeting>

    /** 만남 폼을 임시저장 */
    suspend fun saveMeetingDraft(form: MeetingForm)

    /** 만남 등록 (백엔드 API 연동) */
    suspend fun registerMeeting(token: String, form: MeetingForm): Result<MeetingDetail>

    /** 만남 목록 조회 (페이징) */
    suspend fun getMeetings(
        page: Int = 0,
        size: Int = 10,
        status: String? = null,
        keyword: String? = null
    ): Result<PageResponse<Meeting>>

    /** 만남 상세 조회 */
    suspend fun getMeetingDetail(meetingId: String): Result<MeetingDetail>

    /** 만남 참가 */
    suspend fun joinMeeting(token: String, meetingId: String): Result<Unit>

    /** 만남 참가 취소 */
    suspend fun leaveMeeting(token: String, meetingId: String): Result<Unit>

    /** 만남 상태 변경 */
    suspend fun updateMeetingStatus(token: String, meetingId: String, status: String): Result<Unit>
}
