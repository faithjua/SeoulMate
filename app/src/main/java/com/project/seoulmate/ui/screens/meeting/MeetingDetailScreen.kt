package com.project.seoulmate.ui.screens.meeting

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
        Scaffold(
            bottomBar = { 
                DetailBottomBar(
                    isFavorite = isFavorite,
                    onFavoriteClick = { viewModel.toggleFavorite() }
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
                InfoSection(detail = detail)

                Divider(color = Color(0xFFF0F0F0), thickness = 8.dp)

                // 설명 섹션
                DescriptionSection(
                    detail = detail,
                    onReportMeeting = {
                        // 게시글 신고 (임시로 '게시글 문제' 사유 사용)
                        viewModel.reportUser(reason = "POST_CONTENT", description = "게시글 부적절")
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
                        viewModel.reportUser(reason = "USER_BEHAVIOR", description = "사용자 부적절")
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

@Composable
fun HeaderSection(meeting: Meeting, onBackClick: () -> Unit, onSearchClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // 배경 이미지
        if (meeting.imageUrl != null) {
            AsyncImage(
                model = meeting.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = painterResource(id = if (meeting.imageRes != 0) meeting.imageRes else R.drawable.img_recommend_1),
                placeholder = painterResource(id = if (meeting.imageRes != 0) meeting.imageRes else R.drawable.img_recommend_1)
            )
        } else {
            Image(
                painter = painterResource(id = if (meeting.imageRes != 0) meeting.imageRes else R.drawable.img_recommend_1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
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

        // 뷰페이저 인디케이터 형태 (가상)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(6) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == 1) 8.dp else 6.dp)
                        .background(
                            color = if (index == 1) Color(0xFF7A6BFF) else Color.White.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun InfoSection(detail: MeetingDetail) {
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
                text = "메이트 정보",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "차단하기",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onBlockUser() }
                )
                Text(
                    text = "신고하기",
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
                contentDescription = "프로필 이미지",
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
    onFavoriteClick: () -> Unit = {}
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
                contentDescription = "찜하기",
                tint = if (isFavorite) Color(0xFFF44336) else Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 쪽지 버튼
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D8D8D)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "쪽지", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 예약 요청 버튼
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C60FD)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "예약 요청", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
