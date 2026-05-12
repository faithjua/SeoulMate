package com.project.seoulmate.ui.screens.wishlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.project.seoulmate.R
import com.project.seoulmate.config.AppConfig
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.ui.components.BottomNavigationBar
import com.project.seoulmate.ui.navigation.Screen
import com.project.seoulmate.ui.util.displayCategoryName
import com.project.seoulmate.ui.util.displayCongestionLabel
import com.project.seoulmate.ui.util.displayMeetingTag
import com.project.seoulmate.ui.util.tagColor
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    navController: NavController,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val meetings by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val filterCategories by viewModel.filterCategories.collectAsStateWithLifecycle()
    val congestionLevels by viewModel.congestionLevels.collectAsStateWithLifecycle()

    // 하단 네비게이션 선택 상태 (찜 화면이므로 1)
    val selectedBottomItem = 1

    // 알림 개수를 저장하는 상태 변수 (현재 사용 안 함)
    // var unreadAlarmCount by remember { mutableStateOf(2) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedBottomItem,
                onItemSelected = { index ->
                    // BottomNavigationBar의 실제 렌더 순서: Home(0), Favorite(1), Add(2), Profile(3).
                    // (Message는 주석 처리되어 렌더되지 않음 — 코드의 "// index 4" 주석은 오기재.)
                    when (index) {
                        0 -> navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                        2 -> navController.navigate(Screen.AddMeeting.createRoute())
                        3 -> if (!AppConfig.IS_PRODUCTION) {
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Top Bar Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.wishlist_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // 번역 아이콘과 알람 아이콘 숨김처리
                /*
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* TODO: Translate */ }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_translate),
                            contentDescription = stringResource(id = R.string.wishlist_translate),
                            contentScale = ContentScale.None
                        )
                    }

                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(
                            onClick = { navController.navigate(Screen.Notifications.route) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = stringResource(id = R.string.wishlist_notification),
                                modifier = Modifier.size(28.dp),
                                tint = Color.Black
                            )
                        }

                        // Badge (알림 개수가 0보다 클 때만 배지를 보여줍니다)
                        if (unreadAlarmCount > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp, end = 6.dp)
                                    .size(16.dp)
                                    .background(color = Color(0xFFFF6B6B), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$unreadAlarmCount",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                */
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Filter Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 카테고리 필터 (카탈로그 API 데이터 사용)
                // options/selection의 raw 값은 한글(백엔드 type)을 유지하고, 화면에는 localized 표시.
                if (filterCategories.isNotEmpty()) {
                    FilterChipItem(
                        text = stringResource(id = R.string.home_filter_category),
                        options = listOf("전체") + filterCategories,
                        labelFor = { raw ->
                            if (raw == "전체") stringResource(id = R.string.wishlist_filter_all)
                            else displayCategoryName(raw)
                        }
                    )
                }

                // 혼잡도 필터 (카탈로그 API 데이터 사용)
                if (congestionLevels.isNotEmpty()) {
                    FilterChipItem(
                        text = stringResource(id = R.string.wishlist_filter_congestion),
                        options = congestionLevels.map { it.label },
                        labelFor = { displayCongestionLabel(it) }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Settings Icon Button
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { /* TODO: Filter popup */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = stringResource(id = R.string.wishlist_filter_setting),
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Count
            Text(
                text = stringResource(id = R.string.wishlist_total_count, meetings.size),
                modifier = Modifier.padding(horizontal = 24.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Grid List
            if (isLoading && meetings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6C60FD))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(meetings) { meeting ->
                        WishlistCard(
                            meeting = meeting,
                            onClick = { navController.navigate("meeting_detail/${meeting.id}") },
                            onRemoveFavorite = { viewModel.removeFavorite(meeting.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    text: String,
    options: List<String> = emptyList(), // raw 옵션 값 (백엔드 type, 한글)
    labelFor: @Composable (String) -> String = { it } // 화면 표시용 변환
) {
    // 드롭다운 메뉴가 열려있는지 여부를 저장하는 상태
    var expanded by remember { mutableStateOf(false) }
    // 현재 선택된 raw 값 (null이면 미선택 → text 라벨 표시)
    var selectedRaw by remember { mutableStateOf<String?>(null) }
    val displayText = selectedRaw?.let { labelFor(it) } ?: text

    Box {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
            modifier = Modifier
                .height(36.dp)
                .clickable { expanded = true } // 버튼을 누르면 드롭다운이 열림
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayText,
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
                    text = { Text(labelFor(option)) },
                    onClick = {
                        selectedRaw = option
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WishlistCard(
    meeting: Meeting,
    onClick: () -> Unit,
    onRemoveFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        // Top: Image with Heart & Tags
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Square shape based on design
                .clip(RoundedCornerShape(12.dp))
        ) {
            // 서버 이미지 URL 우선 사용, 없으면 로컬 리소스 사용
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

            // Heart Icon (Top Right)
            IconButton(
                onClick = { onRemoveFavorite() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = stringResource(id = R.string.wishlist_cancel),
                    tint = Color(0xFFFF6B6B), // Red-pinkish color
                    modifier = Modifier.size(28.dp)
                )
            }

            // Tags (Bottom Left). 영문 라벨이 길어지면 chip이 줄바꿈되지 않도록
            // 각 Text는 단일 라인으로 고정하고, 여러 태그는 FlowRow로 자연스럽게 줄바꿈.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                maxItemsInEachRow = Int.MAX_VALUE,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .fillMaxWidth(0.85f)
            ) {
                meeting.tags.take(3).forEach { tag ->
                    Surface(
                        color = tagColor(tag),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = displayMeetingTag(tag),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = meeting.title,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Date / Time
        Text(
            text = meeting.time,
            color = Color(0xFF888888),
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Price & Rating
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = R.string.wishlist_expected_price, meeting.price),
                color = Color(0xFF888888),
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = stringResource(id = R.string.wishlist_rating),
                tint = Color(0xFF888888),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = String.format("%.1f", meeting.ratingAvg ?: 0.0),
                color = Color(0xFF888888),
                fontSize = 13.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WishlistScreenPreview() {
    WishlistScreen(navController = rememberNavController())
}