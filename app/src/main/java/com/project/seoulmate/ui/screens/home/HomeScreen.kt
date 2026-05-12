package com.project.seoulmate.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.project.seoulmate.R
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.ui.components.*
import com.project.seoulmate.ui.navigation.Screen
import com.project.seoulmate.ui.theme.SeoulMateTheme
import com.project.seoulmate.config.AppConfig

/**
 * 홈 화면 Composable
 *
 * @param navController 화면 이동을 위한 NavController
 * @param viewModel Hilt가 자동으로 주입하는 HomeViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // StateFlow를 Compose State로 수집
    // collectAsStateWithLifecycle: 화면이 보이지 않을 때(백그라운드) 수집 중단 → 배터리 절약
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val recentMeetings by viewModel.recentMeetings.collectAsStateWithLifecycle()
    val todayMeetings by viewModel.todayMeetings.collectAsStateWithLifecycle()
    val lowCongestionMeetings by viewModel.lowCongestionMeetings.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    // 카탈로그 데이터
    val filterCategories by viewModel.filterCategories.collectAsStateWithLifecycle()
    val congestionLevels by viewModel.congestionLevels.collectAsStateWithLifecycle()
    val selectedFilterCategory by viewModel.selectedFilterCategory.collectAsStateWithLifecycle()
    val selectedCongestion by viewModel.selectedCongestion.collectAsStateWithLifecycle()

    // 하단 네비게이션 선택 상태 (네비게이션 바 전용 UI 상태, 간단하므로 여기서 관리)
    var selectedBottomItem by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedBottomItem,
                onItemSelected = { index ->
                    when (index) {
                        1 -> navController.navigate(Screen.Wishlist.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        2 -> navController.navigate(Screen.AddMeeting.createRoute())
                        // 개발 모드에서만 쪽지(3)/프로필(4) 탭 노출
                        3 -> if (!AppConfig.IS_PRODUCTION) { /* 쪽지 - TODO */ }
                        4 -> if (!AppConfig.IS_PRODUCTION) {
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        else -> selectedBottomItem = index
                    }
                }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. 상단 바 (로고 + 번역 버튼 + 알림 버튼)
            item(span = { GridItemSpan(2) }) {
                TopBar(
                    onTranslateClick = { /* TODO: 번역 기능 */ },
                    onNotificationClick = {
                        navController.navigate(Screen.Notifications.route)
                    }
                )
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 2. 검색 바
            item(span = { GridItemSpan(2) }) {
                SearchBar(
                    onSearchClick = { navController.navigate(Screen.Search.route) }
                )
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. 카테고리 섹션
            item(span = { GridItemSpan(2) }) {
                CategorySection(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategoryClick = { category ->
                        viewModel.onCategorySelected(category)
                    }
                )
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (selectedCategory?.isAllMenu == true || selectedCategory == null) {
                // "전체메뉴" 인 경우 기존 가로 스크롤 섹션 노출
                item(span = { GridItemSpan(2) }) {
                    RecommendationSection(
                        title = "최근 올라온 만남",
                        modifier = Modifier.fillMaxWidth(),
                        meetings = recentMeetings,
                        onSeeAllClick = { /* TODO: 전체보기 페이지 이동 */ },
                        onMeetingClick = { meetingId ->
                            navController.navigate(Screen.MeetingDetail.createRoute(meetingId))
                        },
                        onFavoriteClick = { meetingId, isFavorited ->
                            viewModel.toggleFavorite(meetingId, isFavorited)
                        }
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    RecommendationSection(
                        title = "당일 만남",
                        modifier = Modifier.fillMaxWidth(),
                        meetings = todayMeetings,
                        onSeeAllClick = { /* TODO */ },
                        onMeetingClick = { meetingId ->
                            navController.navigate(Screen.MeetingDetail.createRoute(meetingId))
                        },
                        onFavoriteClick = { meetingId, isFavorited ->
                            viewModel.toggleFavorite(meetingId, isFavorited)
                        }
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    RecommendationSection(
                        title = "혼잡도 낮은 만남",
                        modifier = Modifier.fillMaxWidth(),
                        meetings = lowCongestionMeetings,
                        onSeeAllClick = { /* TODO */ },
                        onMeetingClick = { meetingId ->
                            navController.navigate(Screen.MeetingDetail.createRoute(meetingId))
                        },
                        onFavoriteClick = { meetingId, isFavorited ->
                            viewModel.toggleFavorite(meetingId, isFavorited)
                        }
                    )
                }
            } else {
                // 개별 카테고리 선택 시 2열 세로 스크롤리뷰 및 상단 드롭다운 필터바 노출
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 카테고리 드롭다운 필터 (카탈로그 API 데이터 사용)
                        if (filterCategories.isNotEmpty()) {
                            FilterChipItem(
                                text = "카테고리",
                                options = filterCategories,
                                selectedOption = selectedFilterCategory ?: "카테고리",
                                onOptionSelected = { selectedName ->
                                    // "당일만남" 선택 시 null 전달 (백엔드에서 today=true로 처리)
                                    val categoryParam = if (selectedName == "당일만남") null else selectedName
                                    viewModel.onFilterCategorySelected(categoryParam)
                                }
                            )
                        }

                        // 혼잡도 드롭다운 필터 (카탈로그 API 데이터 사용)
                        if (congestionLevels.isNotEmpty()) {
                            FilterChipItem(
                                text = "혼잡도",
                                options = congestionLevels.map { it.label },
                                selectedOption = congestionLevels.find { it.code == selectedCongestion }?.label ?: "혼잡도",
                                onOptionSelected = { selectedLabel ->
                                    val congestionOption = congestionLevels.find { it.label == selectedLabel }
                                    // "전체" 선택 시 null 전달
                                    val congestionParam = if (congestionOption?.code == "ALL") null else congestionOption?.code
                                    viewModel.onCongestionSelected(congestionParam)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // 설정 조정 튜닝 아이콘 (Wishlist 디자인과 일관성)
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { /* Filter popup */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = "필터 설정",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // 총 건수 텍스트 노출
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "만남 ${recentMeetings.size}개",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }

                // 세로형 2열 모임 그리드 카드들 노출
                items(recentMeetings) { meeting ->
                    MeetingGridCard(
                        meeting = meeting,
                        onClick = {
                            navController.navigate(Screen.MeetingDetail.createRoute(meeting.id))
                        },
                        onFavoriteToggle = {
                            viewModel.toggleFavorite(meeting.id, meeting.isFavorited)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    text: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
            modifier = Modifier
                .height(36.dp)
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedOption,
                    fontSize = 13.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Black
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MeetingGridCard(
    meeting: Meeting,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
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

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (meeting.isFavorited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        tint = if (meeting.isFavorited) Color(0xFFFF6B6B) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    meeting.tags.forEach { tag ->
                        val cleanTag = if (tag.startsWith("#")) tag.removePrefix("#") else tag
                        val isCongestion = cleanTag == "여유" || cleanTag == "보통" || cleanTag == "약간 붐빔" || cleanTag == "붐빔" || cleanTag == "혼잡" || cleanTag == "정보 없음"
                        val tagColor = when (cleanTag) {
                            "여유" -> Color(0xFF6CF0A0)
                            "보통" -> Color(0xFF4A90E2)
                            "약간 붐빔" -> Color(0xFFFF9500)
                            "붐빔", "혼잡" -> Color(0xFFFF6B6B)
                            "정보 없음" -> Color(0xFF9E9E9E)
                            else -> Color(0xFF6C60FD)
                        }

                        Surface(
                            color = tagColor,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isCongestion) cleanTag else "#$cleanTag",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = meeting.title,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = meeting.time ?: "",
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "예상 " + (meeting.price ?: ""),
                        color = Color(0xFF888888),
                        fontSize = 11.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", meeting.ratingAvg ?: 0.0),
                            color = Color(0xFF888888),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

