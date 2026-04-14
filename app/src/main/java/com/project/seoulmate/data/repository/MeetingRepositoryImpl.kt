package com.project.seoulmate.data.repository

import com.project.seoulmate.R
import com.project.seoulmate.data.model.*
import com.project.seoulmate.data.remote.MeetingApi
import com.project.seoulmate.data.remote.PageResponse
import timber.log.Timber
import javax.inject.Inject

/**
 * MeetingRepository의 실제 구현체.
 *
 * @Inject constructor — Hilt가 이 클래스를 자동으로 생성할 수 있게 함
 */
class MeetingRepositoryImpl @Inject constructor(
    private val meetingApi: MeetingApi
) : MeetingRepository {

    // 카테고리 목록 반환
    override fun getCategories(): List<Category> = listOf(
        Category(id = "all", name = "전체메뉴", isAllMenu = true),
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
     * 최근 본 만남 목록 반환 (더미 데이터)
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
        // TODO: Room DB로 임시저장 구현
        Timber.d("Draft saved: ${form.name}")
    }

    /**
     * 만남 등록 (백엔드 API 연동)
     */
    override suspend fun registerMeeting(token: String, form: MeetingForm): Result<MeetingDetail> {
        return try {
            val request = form.toCreateRequest()
            val response = meetingApi.createMeeting("Bearer $token", request)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data.toMeetingDetail())
                } else {
                    Result.failure(Exception("응답 데이터가 없습니다"))
                }
            } else {
                val errorMsg = response.body()?.message ?: "만남 등록 실패"
                Timber.e("Meeting registration failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Meeting registration error")
            Result.failure(e)
        }
    }

    /**
     * 만남 목록 조회 (페이징)
     */
    override suspend fun getMeetings(
        page: Int,
        size: Int,
        status: String?,
        keyword: String?
    ): Result<PageResponse<Meeting>> {
        return try {
            val response = meetingApi.getMeetings(page, size, status, keyword)

            if (response.isSuccessful && response.body()?.success == true) {
                val pageData = response.body()?.data
                if (pageData != null) {
                    val meetings = pageData.content.map { it.toMeeting() }
                    val resultPage = PageResponse(
                        content = meetings,
                        page = pageData.page,
                        size = pageData.size,
                        totalElements = pageData.totalElements,
                        totalPages = pageData.totalPages,
                        first = pageData.first,
                        last = pageData.last
                    )
                    Result.success(resultPage)
                } else {
                    Result.failure(Exception("응답 데이터가 없습니다"))
                }
            } else {
                val errorMsg = response.body()?.message ?: "만남 목록 조회 실패"
                Timber.e("Get meetings failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Get meetings error")
            Result.failure(e)
        }
    }

    /**
     * 만남 상세 조회
     */
    override suspend fun getMeetingDetail(meetingId: String): Result<MeetingDetail> {
        return try {
            val response = meetingApi.getMeetingDetail(meetingId.toLongOrNull() ?: 0L)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data.toMeetingDetail())
                } else {
                    Result.failure(Exception("응답 데이터가 없습니다"))
                }
            } else {
                val errorMsg = response.body()?.message ?: "만남 상세 조회 실패"
                Timber.e("Get meeting detail failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Get meeting detail error")
            Result.failure(e)
        }
    }

    override suspend fun joinMeeting(token: String, meetingId: String): Result<Unit> {
        return try {
            val response = meetingApi.joinMeeting("Bearer $token", meetingId.toLongOrNull() ?: 0L)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: "참가 신청 실패"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveMeeting(token: String, meetingId: String): Result<Unit> {
        return try {
            val response = meetingApi.leaveMeeting("Bearer $token", meetingId.toLongOrNull() ?: 0L)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: "참가 취소 실패"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMeetingStatus(token: String, meetingId: String, status: String): Result<Unit> {
        return try {
            val response = meetingApi.updateMeetingStatus("Bearer $token", meetingId.toLongOrNull() ?: 0L, status)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: "상태 변경 실패"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * MeetingListResponse를 UI Meeting 모델로 변환
 */
private fun MeetingListResponse.toMeeting(): Meeting {
    return Meeting(
        id = this.id.toString(),
        title = this.title,
        time = this.schedule ?: "",
        price = this.estimatedCost?.let { "₩$it" } ?: "가격 미정",
        rating = "0.0", // 리스트 응답에 평점이 없으므로 기본값 처리
        imageRes = R.drawable.img_recommend_1, // thumbnailUrl 처리는 추후 Coil 적용 시 수정
        tags = buildList {
            this@toMeeting.congestionLevel?.let { add(it) }
            addAll(this@toMeeting.tags.map { "#$it" })
        }
    )
}

/**
 * MeetingDetailResponse를 UI MeetingDetail 모델로 변환
 */
private fun MeetingDetailResponse.toMeetingDetail(): MeetingDetail {
    val meeting = Meeting(
        id = this.id.toString(),
        title = this.title,
        time = this.schedule ?: "",
        price = this.estimatedCost?.let { "₩$it" } ?: "가격 미정",
        rating = this.host?.rating?.toString() ?: "0.0",
        imageRes = R.drawable.img_recommend_1, // Coil 적용 시 imageUrls.firstOrNull() 활용
        tags = this.tags.map { "#$it" }
    )

    val coursePoints = this.courses.sortedBy { it.order }.mapIndexed { index, course ->
        CoursePoint(
            name = course.name,
            isStart = index == 0,
            isEnd = index == this.courses.size - 1,
            lat = course.latitude,
            lng = course.longitude
        )
    }

    val mateInfo = this.host?.let {
        MateInfo(
            name = it.nickname,
            profileRes = R.drawable.img_recommend_1, // profileImage 활용
            rating = it.rating?.toString() ?: "0.0",
            reviewCount = it.reviewCount,
            bio = it.bio ?: "",
            isVerified = it.isVerified
        )
    } ?: MateInfo(
        name = "Unknown",
        profileRes = R.drawable.img_recommend_1,
        rating = "0.0",
        reviewCount = 0,
        bio = "",
        isVerified = false
    )

    return MeetingDetail(
        meeting = meeting,
        location = this.region ?: "",
        timeElapsed = "", // 클라이언트 계산 필요 시 추가
        dateAndTime = this.schedule ?: "",
        description = this.description,
        courses = coursePoints,
        mateInfo = mateInfo,
        mateOtherMeetings = emptyList()
    )
}
