package com.project.seoulmate.ui.screens.meeting

import com.project.seoulmate.config.AppConfig
import com.project.seoulmate.R
import android.widget.Toast
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.PathOverlay
import com.naver.maps.map.compose.rememberCameraPositionState
import com.project.seoulmate.data.model.*
import com.project.seoulmate.ui.components.RecommendationCard
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MeetingDetailScreen(
    navController: NavHostController,
    viewModel: MeetingDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 메이트 신청 다이얼로그 상태
    var showApplicationDialog by remember { mutableStateOf(false) }

    // 신고/차단 결과에 따른 Toast 노출
    LaunchedEffect(viewModel.userActionEvent) {
        viewModel.userActionEvent.collectLatest { event ->
            when (event) {
                is MeetingDetailViewModel.UserActionResult.Success -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is MeetingDetailViewModel.UserActionResult.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (uiState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6C60FD))
        }
    } else {
        val detail = uiState!!
        val reportPostDesc = stringResource(id = R.string.meeting_report_post_inappropriate)
        val reportUserDesc = stringResource(id = R.string.meeting_report_user_inappropriate)
        // 메이트 신청 다이얼로그
        if (showApplicationDialog) {
            ApplicationMessageDialog(
                onDismiss = { showApplicationDialog = false },
                onConfirm = { message ->
                    showApplicationDialog = false
                    viewModel.applyForMeeting(message)
                }
            )
        }

        Scaffold(
            bottomBar = {
                DetailBottomBar(
                    isFavorite = isFavorite,
                    onFavoriteClick = { viewModel.toggleFavorite() },
                    onApplyClick = { showApplicationDialog = true }
                )
            },
            containerColor = Color.White
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // 상단 이미지 & TopBar
                HeaderSection(
                    meeting = detail.meeting,
                    onBackClick = { navController.popBackStack() },
                    onSearchClick = { /* TODO */ }
                )

                // 기본 정보 섹션
                InfoSection(
                    detail = detail,
                    onEditClick = if (detail.isHost) {
                        { /* TODO: 수정 화면으로 이동 */ }
                    } else null,
                    onDeleteClick = if (detail.isHost) {
                        { viewModel.deleteMeeting { navController.popBackStack() } }
                    } else null,
                    onStatusChange = if (detail.isHost) {
                        { status -> viewModel.updateMeetingStatus(status) }
                    } else null
                )

                Divider(color = Color(0xFFF0F0F0), thickness = 8.dp)

                // 설명 섹션
                DescriptionSection(
                    detail = detail,
                    onReportMeeting = {
                        // 게시글 신고 (임시로 '게시글 문제' 사유 사용)
                        viewModel.reportUser(reason = "POST_CONTENT", description = reportPostDesc)
                    }
                )

                Divider(color = Color(0xFFF0F0F0), thickness = 8.dp)

                // 코스 섹션
                CourseSection(courses = detail.courses)

                Divider(color = Color(0xFFF0F0F0), thickness = 8.dp)

                // 메이트 정보 섹션
                MateInfoSection(
                    mateInfo = detail.mateInfo,
                    otherMeetings = detail.mateOtherMeetings,
                    onReportUser = {
                        // 사용자 신고 (임시로 '기타' 사후 사용)
                        viewModel.reportUser(reason = "USER_BEHAVIOR", description = reportUserDesc)
                    },
                    onBlockUser = { viewModel.blockUser() },
                    onMeetingClick = { meetingId ->
                        // 다른 만남 공고 클릭 시 처리
                        navController.navigate("meeting_detail/$meetingId")
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HeaderSection(meeting: Meeting, onBackClick: () -> Unit, onSearchClick: () -> Unit) {
    // 이미지가 없으면 기본 이미지 사용
    val images = if (meeting.imageUrls.isNotEmpty()) {
        meeting.imageUrls
    } else if (meeting.imageRes != 0) {
        listOf("drawable://${meeting.imageRes}")
    } else {
        listOf("drawable://${R.drawable.img_recommend_1}")
    }

    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // 이미지 페이저
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val imageUrl = images[page]

            if (imageUrl.startsWith("http")) {
                // 서버 이미지 (S3 URL)
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "만남 이미지 ${page + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.img_recommend_1)
                )
            } else if (imageUrl.startsWith("drawable://")) {
                // 로컬 drawable 리소스
                val resId = imageUrl.removePrefix("drawable://").toIntOrNull() ?: R.drawable.img_recommend_1
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "만남 이미지 ${page + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // TopBar (투명)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "검색",
                    tint = Color.White
                )
            }
        }

        // 페이지 인디케이터 (동적)
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(images.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                            .background(
                                color = if (index == pagerState.currentPage) Color(0xFF7A6BFF) else Color.White.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun InfoSection(
    detail: MeetingDetail,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onStatusChange: ((String) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${detail.meeting.title}  ",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = detail.location,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 호스트일 경우 관리 버튼 표시
            if (onEditClick != null || onDeleteClick != null || onStatusChange != null) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 상태 변경 버튼들 (호스트 전용)
                    if (onStatusChange != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val currentStatus = detail.status
                            val maxMembers = detail.maxMembers
                            val currentMembers = detail.currentMembers

                            // 정원 정보 표시
                            if (maxMembers != null && currentMembers != null) {
                                Text(
                                    text = stringResource(id = R.string.meeting_capacity_format, currentMembers, maxMembers),
                                    fontSize = 11.sp,
                                    color = if (currentMembers >= maxMembers) Color.Red else Color.Gray,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }

                            when (currentStatus) {
                                "OPEN" -> {
                                    OutlinedButton(
                                        onClick = { onStatusChange("CLOSED") },
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFF9800))
                                    ) {
                                        Text("마감", fontSize = 11.sp, color = Color(0xFFFF9800))
                                    }
                                    OutlinedButton(
                                        onClick = { onStatusChange("COMPLETED") },
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        border = BorderStroke(1.dp, Color(0xFF4CAF50))
                                    ) {
                                        Text("완료", fontSize = 11.sp, color = Color(0xFF4CAF50))
                                    }
                                }
                                "CLOSED" -> {
                                    OutlinedButton(
                                        onClick = { onStatusChange("OPEN") },
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        border = BorderStroke(1.dp, Color(0xFF6C60FD))
                                    ) {
                                        Text("재개", fontSize = 11.sp, color = Color(0xFF6C60FD))
                                    }
                                    OutlinedButton(
                                        onClick = { onStatusChange("COMPLETED") },
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        border = BorderStroke(1.dp, Color(0xFF4CAF50))
                                    ) {
                                        Text("완료", fontSize = 11.sp, color = Color(0xFF4CAF50))
                                    }
                                }
                                "COMPLETED" -> {
                                    Text(
                                        text = "완료됨",
                                        fontSize = 11.sp,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // 수정/삭제 버튼
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (onEditClick != null) {
                            OutlinedButton(
                                onClick = onEditClick,
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                border = BorderStroke(1.dp, Color(0xFF6C60FD))
                            ) {
                                Text("수정", fontSize = 11.sp, color = Color(0xFF6C60FD))
                            }
                        }
                        if (onDeleteClick != null) {
                            OutlinedButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                border = BorderStroke(1.dp, Color.Red)
                            ) {
                                Text("삭제", fontSize = 11.sp, color = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = detail.meeting.price,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = detail.timeElapsed,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                detail.meeting.tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFEDEDED)),
                        color = Color.White
                    ) {
                        Text(
                            text = tag,
                            fontSize = 12.sp,
                            color = Color(0xFF6C60FD),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "좋아요",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DescriptionSection(detail: MeetingDetail, onReportMeeting: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "일시/시간 : ${detail.dateAndTime}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "만남 소개 : ${detail.description}",
            fontSize = 14.sp,
            color = Color(0xFF4A4A4A),
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "이 게시글 신고하기",
            fontSize = 12.sp,
            color = Color.Gray,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { onReportMeeting() }
        )
    }
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun CourseSection(courses: List<CoursePoint>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.meeting_course),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (courses.isEmpty()) {
            Text(
                text = "등록된 코스가 없습니다",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            return@Column
        }

        // 타임라인 UI
        courses.forEachIndexed { index, course ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 아이콘 및 선
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    if (course.isStart) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = stringResource(id = R.string.meeting_start),
                            tint = Color(0xFF6C60FD),
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (course.isEnd) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = stringResource(id = R.string.meeting_end),
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFE0E0E0), CircleShape)
                                .padding(4.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF6C60FD), RoundedCornerShape(4.dp))
                        )
                    }

                    if (index < courses.size - 1) {
                        // 세로 선 (점선 대신 실선으로 간단히 구현)
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(32.dp)
                                .padding(vertical = 4.dp)
                                .background(Color(0xFF6C60FD))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                //  텍스트 영역: 장소 이름과 (위치 미지원) 안내 문구
                Column {
                    Text(
                        text = course.name,
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    // 좌표가 null이거나 0.0이면 위치 미지원 안내 표시
                    val isInvalidLocation = course.lat == null || course.lat == 0.0 || course.lng == null || course.lng == 0.0
                    if (isInvalidLocation) {
                        Text(
                            text = "지도 위치 미지원 장소",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        //  지도 영역 방어 로직 (크래시 방지)
        // 1. 유효한(정상적인) 좌표만 필터링합니다.
        val validCourses = courses.filter {
            it.lat != null && it.lat != 0.0 && it.lng != null && it.lng != 0.0
        }

        // 2. 유효한 좌표가 있을 때만 지도를 그립니다.
        if (validCourses.isNotEmpty()) {
            // 필터링된 애들로만 평균을 구해야 에러(NaN)가 나지 않습니다.
            val centerLat = validCourses.map { it.lat!! }.average()
            val centerLng = validCourses.map { it.lng!! }.average()

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition(LatLng(centerLat, centerLng), 13.0)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                NaverMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    // 유효한 애들만 마커 표시
                    validCourses.forEach { course ->
                        Marker(
                            state = MarkerState(position = LatLng(course.lat!!, course.lng!!)),
                            captionText = course.name
                        )
                    }
                }
            }
        } else {
            // 모든 장소가 유효한 좌표가 없을 경우 (지도를 아예 숨기거나 안내 박스를 보여줌)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFF8F8F8), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "표시할 수 있는 지도 위치가 없습니다.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    }
}
/*
@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun CourseSection(courses: List<CoursePoint>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "코스",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (courses.isEmpty()) {
            Text(
                text = "등록된 코스가 없습니다",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            return@Column
        }

        // 타임라인 UI
        courses.forEachIndexed { index, course ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 아이콘 및 선
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    if (course.isStart) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "시작",
                            tint = Color(0xFF6C60FD),
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (course.isEnd) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "종료",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFE0E0E0), CircleShape)
                                .padding(4.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF6C60FD), RoundedCornerShape(4.dp))
                        )
                    }

                    if (index < courses.size - 1) {
                        // 세로 선 (점선 대신 실선으로 간단히 구현)
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(32.dp)
                                .padding(vertical = 4.dp)
                                .background(Color(0xFF6C60FD))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = course.name,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 네이버 지도 영역
        if (courses.isNotEmpty()) {
            val centerLat = courses.map { it.lat }.average()
            val centerLng = courses.map { it.lng }.average()
            
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition(LatLng(centerLat, centerLng), 13.0)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                NaverMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    // 마커 표시
                    courses.forEach { course ->
                        Marker(
                            state = MarkerState(position = LatLng(course.lat, course.lng)),
                            captionText = course.name
                        )
                    }
                    
                    // PathOverlay 가능 여부는 naver-map-compose API 확인 필요
                    // 간단히 마커만 표시
                }
            }
        }
    }
}

 */

@Composable
fun MateInfoSection(
    mateInfo: MateInfo, 
    otherMeetings: List<Meeting>, 
    onReportUser: () -> Unit,
    onBlockUser: () -> Unit,
    onMeetingClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.meeting_mate_info),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(id = R.string.meeting_block),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onBlockUser() }
                )
                Text(
                    text = stringResource(id = R.string.meeting_report),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onReportUser() }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 프로필 정보
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = mateInfo.profileRes),
                contentDescription = stringResource(id = R.string.meeting_profile_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mateInfo.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    if (mateInfo.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "인증됨",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "평점",
                        tint = Color(0xFF6C60FD),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${mateInfo.rating} (${mateInfo.reviewCount})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mateInfo.bio,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 다른 만남 가로 리스트
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            items(otherMeetings) { meeting ->
                RecommendationCard(
                    meeting = meeting,
                    onClick = onMeetingClick
                )
            }
        }
    }
}

@Composable
fun DetailBottomBar(
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onApplyClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 좋아요 아이콘 버튼
        OutlinedButton(
            onClick = onFavoriteClick,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(id = R.string.meeting_favorite),
                tint = if (isFavorite) Color(0xFFF44336) else Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 메이트 신청 버튼
        Button(
            onClick = onApplyClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C60FD)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "메이트 신청", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

    }
}

/**
 * 메이트 신청 메시지 입력 다이얼로그
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationMessageDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var message by remember { mutableStateOf("") }
    val maxLength = 200

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "메이트 신청",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "호스트에게 전달할 메시지를 입력해주세요",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = {
                        if (it.length <= maxLength) {
                            message = it
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text(
                            "예: 안녕하세요! 함께 가고 싶어서 신청합니다 :)",
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )
                    },
                    supportingText = {
                        Text(
                            "${message.length} / $maxLength",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C60FD),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (message.isNotBlank()) {
                        onConfirm(message.trim())
                    }
                },
                enabled = message.isNotBlank()
            ) {
                Text(
                    "신청하기",
                    color = if (message.isNotBlank()) Color(0xFF6C60FD) else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color.Gray)
            }
        }
    )
}
