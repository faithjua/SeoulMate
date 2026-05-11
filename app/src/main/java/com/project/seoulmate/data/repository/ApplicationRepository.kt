package com.project.seoulmate.data.repository

import com.project.seoulmate.data.remote.ApplicationResponse

/**
 * Application(만남 신청) Repository 인터페이스
 */
interface ApplicationRepository {

    /**
     * 만남 신청
     */
    suspend fun createApplication(token: String, meetupId: Long): Result<ApplicationResponse>

    /**
     * 신청 승인 (호스트)
     */
    suspend fun approveApplication(token: String, meetupId: Long, appId: Long): Result<Unit>

    /**
     * 신청 거절 (호스트)
     */
    suspend fun rejectApplication(token: String, meetupId: Long, appId: Long): Result<Unit>

    /**
     * 신청 목록 조회 (호스트)
     */
    suspend fun getApplications(token: String, meetupId: Long): Result<List<ApplicationResponse>>

    /**
     * 내가 보낸 신청 목록
     */
    suspend fun getMyApplications(token: String): Result<List<ApplicationResponse>>
}
