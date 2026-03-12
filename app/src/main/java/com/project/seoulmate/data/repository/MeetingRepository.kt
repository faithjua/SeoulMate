package com.project.seoulmate.data.repository

import com.project.seoulmate.data.model.Category
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.data.model.MeetingForm

/**
 * MeetingRepository 인터페이스.
 */
interface MeetingRepository {
    /** 카테고리 목록 반환 */
    fun getCategories(): List<Category>

    /** 최근 본 만남 목록 반환 */
    fun getRecentMeetings(): List<Meeting>

    /** 만남 폼을 임시저장 (나중에 suspend로 API 연결) */
    suspend fun saveMeetingDraft(form: MeetingForm)

    /** 만남 등록 (나중에 suspend로 API 연결) */
    suspend fun registerMeeting(form: MeetingForm)
}
