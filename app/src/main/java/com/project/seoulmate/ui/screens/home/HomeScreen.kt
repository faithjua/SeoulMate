package com.project.seoulmate.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
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
                        2 -> navController.navigate(Screen.AddMeeting.route)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. 상단 바 (로고 + 번역 버튼 + 알림 버튼)
                TopBar(
                    onTranslateClick = { /* TODO: 번역 기능 */ },
                    onNotificationClick = {
                        navController.navigate(Screen.Notifications.route)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. 검색 바
                SearchBar(
                    onSearchClick = { navController.navigate(Screen.Search.route) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. 카테고리 섹션
                // ViewModel에서 내려온 categories, selectedCategory 전달
                CategorySection(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategoryClick = { category ->
                        viewModel.onCategorySelected(category)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4. 최근 (올라온)본 만남 섹션
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

                // 5. 당일 만남 섹션
                RecommendationSection(
                    title = "당일 만남",
                    modifier = Modifier.fillMaxWidth(),
                    meetings = todayMeetings, // 오늘 날짜의 만남만 표시
                    onSeeAllClick = { /* TODO */ },
                    onMeetingClick = { meetingId ->
                        navController.navigate(Screen.MeetingDetail.createRoute(meetingId))
                    },
                    onFavoriteClick = { meetingId, isFavorited ->
                        viewModel.toggleFavorite(meetingId, isFavorited)
                    }
                )

                // 6. 혼잡도 관련 만남 섹션
                RecommendationSection(
                    title = "혼잡도 낮은 만남",
                    modifier = Modifier.fillMaxWidth(),
                    meetings = lowCongestionMeetings, // 혼잡도 낮은 만남만 표시
                    onSeeAllClick = { /* TODO */ },
                    onMeetingClick = { meetingId ->
                        navController.navigate(Screen.MeetingDetail.createRoute(meetingId))
                    },
                    onFavoriteClick = { meetingId, isFavorited ->
                        viewModel.toggleFavorite(meetingId, isFavorited)
                    }
                )
/*
                // 7. 지금 인기있는 만남 섹션
                RecommendationSection(
                    title = "지금 인기있는 만남",
                    modifier = Modifier.fillMaxWidth(),
                    meetings = recentMeetings, // 임시로 같은 더미 데이터 사용
                    onSeeAllClick = { /* TODO */ },
                    onMeetingClick = { meetingId ->
                        navController.navigate(Screen.MeetingDetail.createRoute(meetingId))
                    },
                    onFavoriteClick = { meetingId, isFavorited ->
                        viewModel.toggleFavorite(meetingId, isFavorited)
                    }
                )
*/
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
