package com.project.seoulmate.data.repository

import com.project.seoulmate.R
import com.project.seoulmate.data.model.*
import com.project.seoulmate.data.remote.MeetingApi
import com.project.seoulmate.data.remote.PageResponse
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * MeetingRepository의 실제 구현체.
 *
 * @Inject constructor — Hilt가 이 클래스를 자동으로 생성할 수 있게 함
 */
class MeetingRepositoryImpl @Inject constructor(
    private val meetingApi: MeetingApi
) : MeetingRepository {

    override fun getCategories(): List<Category> = listOf(
        Category(id = "all", name = "전체메뉴", isAllMenu = true),
        Category(id = "daily", name = "당일만남", iconRes = R.drawable.ic_today), // 아이콘 적절히 수정 필요
        Category(id = "tourism", name = "관광", iconRes = R.drawable.ic_tourism),
        Category(id = "kpop", name = "K-팝", iconRes = R.drawable.ic_kpop),
        Category(id = "kbeauty", name = "K-뷰티", iconRes = R.drawable.ic_kbeauty),
        Category(id = "shopping", name = "쇼핑", iconRes = R.drawable.ic_shopping),
        Category(id = "food", name = "한식", iconRes = R.drawable.ic_kfood),
        Category(id = "cafe", name = "카페", iconRes = R.drawable.ic_cafe),
        Category(id = "transport", name = "교통 가이드", iconRes = R.drawable.ic_subway),
        // Category(id = "accommodation", name = "숙소/지역", iconRes = R.drawable.ic_accommodation),
        Category(id = "class", name = "클래스", iconRes = R.drawable.ic_class),
        Category(id = "community", name = "커뮤니티", iconRes = R.drawable.ic_community),
        Category(id = "exhibition", name = "전시/공연", iconRes = R.drawable.ic_exhibition),
        Category(id = "safety", name = "안전/생활", iconRes = R.drawable.ic_safety)
    )

    override suspend fun getHomeData(
        category: String?,
        filterCategory: String?,
        congestion: String?
    ): Result<List<Meeting>> {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            val token = user?.let {
                suspendCoroutine<String?> { continuation ->
                    it.getIdToken(false).addOnCompleteListener { task ->
                        if (task.isSuccessful) continuation.resume(task.result?.token)
                        else continuation.resume(null)
                    }
                }
            }

            // "전체메뉴"인 경우 카테고리 필터 제외
            val categoryParam = if (category == "전체메뉴") null else category

            val response = meetingApi.getHomeData(
                token = token?.let { "Bearer $it" },
                category = categoryParam,
                filterCategory = filterCategory,
                congestion = congestion
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val meetups = response.body()?.data?.meetups?.map { it.toMeeting() } ?: emptyList()
                Result.success(meetups)
            } else {
                val errorMsg = response.body()?.message ?: "홈 데이터 조회 실패"
                Timber.e("Get home data failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Get home data error")
            Result.failure(e)
        }
    }

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

    override suspend fun updateMeeting(token: String, meetingId: String, form: MeetingForm): Result<MeetingDetail> {
        return try {
            val request = form.toCreateRequest()
            val response = meetingApi.updateMeeting("Bearer $token", meetingId.toLongOrNull() ?: 0L, request)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data.toMeetingDetail())
                } else {
                    Result.failure(Exception("응답 데이터가 없습니다"))
                }
            } else {
                val errorMsg = response.body()?.message ?: "만남 수정 실패"
                Timber.e("Meeting update failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Meeting update error")
            Result.failure(e)
        }
    }

    override suspend fun deleteMeeting(token: String, meetingId: String): Result<Unit> {
        return try {
            val response = meetingApi.deleteMeeting("Bearer $token", meetingId.toLongOrNull() ?: 0L)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: "만남 삭제 실패"
                Timber.e("Meeting deletion failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Meeting deletion error")
            Result.failure(e)
        }
    }

    override suspend fun getUserMeetups(
        token: String?,
        memberId: Long,
        page: Int,
        size: Int
    ): Result<PageResponse<Meeting>> {
        return try {
            Timber.d("MeetingRepo - getUserMeetups called with memberId: $memberId, page: $page, size: $size")
            Timber.d("MeetingRepo - Token: ${if (token != null) "Bearer ${token.take(20)}..." else "null"}")

            val response = meetingApi.getUserMeetups(
                token = token?.let { "Bearer $it" },
                memberId = memberId,
                page = page,
                size = size
            )

            Timber.d("MeetingRepo - Response code: ${response.code()}")
            Timber.d("MeetingRepo - Response successful: ${response.isSuccessful}")
            Timber.d("MeetingRepo - Response body success: ${response.body()?.success}")

            if (response.isSuccessful && response.body()?.success == true) {
                val pageData = response.body()?.data
                Timber.d("MeetingRepo - Page data: totalElements=${pageData?.totalElements}, content size=${pageData?.content?.size}")

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
                    Timber.d("MeetingRepo - Success! Returning ${meetings.size} meetings")
                    Result.success(resultPage)
                } else {
                    Timber.e("MeetingRepo - Response data is null")
                    Result.failure(Exception("응답 데이터가 없습니다"))
                }
            } else {
                val errorMsg = response.body()?.message ?: "사용자 만남 목록 조회 실패"
                val errorBody = response.errorBody()?.string()
                Timber.e("MeetingRepo - API failed: code=${response.code()}, message=$errorMsg, error=$errorBody")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "MeetingRepo - Exception in getUserMeetups: ${e.message}")
            Result.failure(e)
        }
    }
}

/**
 * MeetingListResponse를 UI Meeting 모델로 변환
 */
private fun MeetingListResponse.toMeeting(): Meeting {
    // 혼잡도 정보 로깅
    Timber.d("MeetingList[${this.id}] - congestionLevel: ${this.congestionLevel}, congestionLabel: ${this.congestionLabel}")

    return Meeting(
        id = this.id.toString(),
        title = this.title,
        time = this.schedule ?: this.meetDate ?: "",
        price = this.estimatedCost?.let { "₩$it" } ?: "미정",
        rating = "0.0", // 리스트 응답에 평점이 없으므로 기본값 처리
        imageRes = R.drawable.img_recommend_1,
        imageUrls = listOfNotNull(this.thumbnailUrl?.takeIf { it.isNotBlank() }),
        tags = buildList {
            // congestionLabel을 우선 사용, 없으면 congestionLevel 사용
            val congestionTag = this@toMeeting.congestionLabel ?: this@toMeeting.congestionLevel
            congestionTag?.let {
                Timber.d("Adding congestion tag: $it")
                add(it)
            }
            addAll(this@toMeeting.tags.map { "#$it" })
        },
        meetDate = this.meetDate,
        ratingAvg = this.ratingAvg ?: 0.0,
        maxMembers = this.maxMembers,
        currentMembers = this.currentMembers,
        status = this.status
    )
}

/**
 * MeetingDetailResponse를 UI MeetingDetail 모델로 변환
 */
private fun MeetingDetailResponse.toMeetingDetail(): MeetingDetail {
    // 혼잡도 정보 로깅
    Timber.d("MeetingDetail[${this.id}] - congestion: ${this.congestion?.label}")

    val meeting = Meeting(
        id = this.id.toString(),
        title = this.title,
        time = this.schedule ?: "",
        price = this.estimatedCost?.let { "₩$it" } ?: "가격 미정",
        rating = this.host?.rating?.toString() ?: "0.0",
        imageRes = R.drawable.img_recommend_1,
        imageUrls = this.imageUrls.filter { it.isNotBlank() }.ifEmpty {
            listOfNotNull(this.thumbnailUrl?.takeIf { it.isNotBlank() })
        },
        tags = buildList {
            // 혼잡도 라벨 추가 (찜 화면 등에서 카드에 표시하기 위해)
            this@toMeetingDetail.congestion?.label?.let {
                Timber.d("Adding congestion label to detail tags: $it")
                add(it)
            }
            addAll(this@toMeetingDetail.tags.map { "#$it" })
        },
        isFavorited = this.isFavorite,
        meetDate = this.meetDate,
        ratingAvg = this.ratingAvg ?: 0.0,
        maxMembers = this.maxMembers,
        currentMembers = this.currentMembers,
        status = this.status
    )

    val places = this.course?.places ?: emptyList()
    Timber.d("MeetingDetail - course places size: ${places.size}")
    val coursePoints = places.sortedBy { it.orderIndex }.mapIndexed { index, place ->
        Timber.d("MeetingDetail - place[$index]: ${place.placeName} (${place.latitude}, ${place.longitude})")
        CoursePoint(
            name = place.placeName,
            isStart = index == 0,
            isEnd = index == places.size - 1,
            lat = place.latitude ?: 0.0,
            lng = place.longitude ?: 0.0
        )
    }

    val mateInfo = MateInfo(
        id = this.host?.id ?: 0L,
        name = this.host?.nickname ?: "Unknown",
        profileRes = R.drawable.ic_profile, // 기본 프로필 아이콘
        rating = this.host?.rating?.toString() ?: "0.0",
        reviewCount = this.host?.reviewCount ?: 0,
        bio = this.host?.bio ?: "",
        isVerified = this.host?.isVerified ?: false
    )

    return MeetingDetail(
        meeting = meeting,
        location = this.region ?: "",
        timeElapsed = "", // TODO: 시간 경과 계산 로직 추가
        dateAndTime = this.schedule ?: this.meetDate ?: "",
        description = this.description,
        courses = coursePoints,
        mateInfo = mateInfo,
        mateOtherMeetings = emptyList(), // TODO: 호스트의 다른 만남 조회 추가
        isHost = this.isHost,
        maxMembers = this.maxMembers,
        currentMembers = this.currentMembers,
        status = this.status
    )
}

/**
 * HomeMeetingResponse를 UI Meeting 모델로 변환
 */
private fun HomeMeetingResponse.toMeeting(): Meeting {
    // 혼잡도 정보 로깅
    Timber.d("HomeMeeting[${this.id}] - congestionLevel: ${this.congestionLevel}, congestionLabel: ${this.congestionLabel}")

    return Meeting(
        id = this.id.toString(),
        title = this.title,
        time = this.schedule ?: this.meetDate ?: "",
        price = this.estimatedCost?.let { "₩$it" } ?: "미정",
        rating = "0.0",
        imageRes = R.drawable.img_recommend_1, // 기본 이미지
        imageUrls = listOfNotNull(this.imageUrl?.takeIf { it.isNotBlank() }),
        tags = buildList {
            this@toMeeting.congestionLabel?.let {
                Timber.d("Adding congestionLabel to tags: $it")
                add(it)
            }
            addAll(this@toMeeting.tags.map { "#$it" })
        },
        isFavorited = this.isFavorited,
        meetDate = this.meetDate,
        ratingAvg = this.ratingAvg ?: 0.0,
        maxMembers = this.maxMembers,
        currentMembers = this.currentMembers,
        status = this.status
    )
}