package com.project.seoulmate.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.project.seoulmate.R
import com.project.seoulmate.config.AppConfig
import com.project.seoulmate.ui.components.BottomNavigationBar
import com.project.seoulmate.ui.components.SuitFontFamily
import com.project.seoulmate.ui.navigation.Screen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(2) } // default to stringResource(id = R.string.profile_tab_info) (Index 2)
    var selectedBottomItem by remember { mutableStateOf(4) } // Profile is index 4
    var showMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // 로그아웃 확인 다이얼로그
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("로그아웃", fontFamily = SuitFontFamily) },
            text = { Text("정말 로그아웃 하시겠습니까?", fontFamily = SuitFontFamily) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        // Firebase 로그아웃
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        // 로그인 화면으로 이동
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("로그아웃", color = Color.Red, fontFamily = SuitFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("취소", fontFamily = SuitFontFamily)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            ProfileTopBar(
                onBackClick = { navController.popBackStack() },
                onMenuClick = { showMenu = true },
                showMenu = showMenu,
                onDismissMenu = { showMenu = false },
                onLogoutClick = {
                    showMenu = false
                    showLogoutDialog = true
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedBottomItem,
                onItemSelected = { index ->
                    when (index) {
                        0 -> navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                        1 -> navController.navigate(Screen.Wishlist.route)
                        2 -> navController.navigate(Screen.AddMeeting.route)
                        else -> selectedBottomItem = index
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // 알맹이(Profile Info)
            ProfileHeader()

            Spacer(modifier = Modifier.height(2.dp))

            // 탭 (만남, 리뷰, 배지)
            val tabs = listOf(stringResource(id = R.string.profile_tab_meeting), stringResource(id = R.string.profile_tab_review), stringResource(id = R.string.profile_tab_badge))
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF6C60FD)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontFamily = SuitFontFamily,
                                fontSize = 16.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) Color(0xFF6C60FD) else Color(0xFF929292)
                            )
                        }
                    )
                }
            }

            // 탭 내용
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> MeetingTabContent(viewModel = viewModel, navController = navController)
                    1 -> ReviewTabContent()
                    2 -> BadgeTabContent(navController = navController)
                }
            }
        }
    }
}

@Composable
fun ProfileTopBar(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = stringResource(id = R.string.profile_back),
            modifier = Modifier
                .size(32.dp)
                .clickable { onBackClick() },
            tint = Color.Black
        )

        Box {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = stringResource(id = R.string.profile_menu),
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onMenuClick() },
                tint = Color.Black
            )

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = onDismissMenu
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "로그아웃",
                            color = Color.Red,
                            fontFamily = SuitFontFamily
                        )
                    },
                    onClick = onLogoutClick
                )
            }
        }
    }
}

@Composable
fun ProfileHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image (Default image used)
            Box(
                modifier = Modifier.size(72.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_default_profile),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
                if (!AppConfig.IS_PRODUCTION) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(id = R.string.profile_edit),
                            tint = Color(0xFF6C60FD),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "소울이",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SuitFontFamily,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_circle),
                    contentDescription = stringResource(id = R.string.profile_verified),
                    tint = Color(0xFF6C60FD),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = stringResource(id = R.string.profile_rating),
                    tint = Color(0xFF6C60FD),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "4.22",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SuitFontFamily,
                    color = Color.Black
                )
                Text(
                    text = " (83)",
                    fontSize = 14.sp,
                    fontFamily = SuitFontFamily,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "인스타 @imseoul 워킹맘\n관광학부 전공으로 개인 투어 맛집입니다 허허\n\n영어, 프랑스어, 한국어 가능합니다^^",
            fontSize = 14.sp,
            fontFamily = SuitFontFamily,
            color = Color.Black,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(id = R.string.profile_view_briefly),
            fontSize = 12.sp,
            fontFamily = SuitFontFamily,
            color = Color.Gray,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { /* TODO */ }
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .width(361.dp)
                .height(36.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(id = R.string.profile_edit_button),
                fontSize = 14.sp,
                fontFamily = SuitFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun BadgeTabContent(navController: NavController) {
    // 12개 배지 정의 (이름, 아이콘 리소스, 배경 톤 컬러)
    val badgeList = remember {
        listOf(
            BadgeData("관광", R.drawable.ic_tourism, Color(0xFFE2F9F3)),
            BadgeData("K-팝", R.drawable.ic_kpop, Color(0xFFE8EAF6)),
            BadgeData("K-뷰티", R.drawable.ic_kbeauty, Color(0xFFFFFDE7)),
            BadgeData("쇼핑", R.drawable.ic_shopping, Color(0xFFE1F5FE)),
            BadgeData("한식", R.drawable.ic_kfood, Color(0xFFFFF3E0)),
            BadgeData("카페", R.drawable.ic_cafe, Color(0xFFE8F5E9)),
            BadgeData("교통 가이드", R.drawable.ic_subway, Color(0xFFF3E5F5)),
            BadgeData("클래스", R.drawable.ic_class, Color(0xFFE0F2F1)),
            BadgeData("스타일", R.drawable.ic_shopping, Color(0xFFEDE7F6)), // 스타일 대용으로 ic_shopping 활용
            BadgeData("커뮤니티", R.drawable.ic_community, Color(0xFFFFEBEE)),
            BadgeData("전시/공연", R.drawable.ic_exhibition, Color(0xFFF1F8E9)),
            BadgeData("안전/생활", R.drawable.ic_safety, Color(0xFFFFF9C4))
        )
    }

    // 선택된 배지 ID/이름을 저장하는 State (토글 가능)
    var selectedBadges by remember { mutableStateOf(setOf<String>()) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 12개 그리드 배지 아이템들
        items(badgeList) { badge ->
            val isSelected = selectedBadges.contains(badge.name)
            BadgeGridItem(
                badge = badge,
                isSelected = isSelected,
                onClick = {
                    selectedBadges = if (isSelected) {
                        selectedBadges - badge.name
                    } else {
                        selectedBadges + badge.name
                    }
                }
            )
        }
    }
}

data class BadgeData(
    val name: String,
    val iconRes: Int,
    val bgColor: Color
)

@Composable
fun BadgeGridItem(
    badge: BadgeData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 아이콘을 담는 둥근 Squircle 박스
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(badge.bgColor)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color(0xFF6C60FD) else Color(0x1F000000),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = badge.iconRes),
                contentDescription = badge.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            
            // 선택되었을 경우 우측 상단에 작은 체크 서클 또는 효과 표시
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF6C60FD),
                        modifier = Modifier.size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewTabContent() {
    // Dummy Data
    val reviews = List(5) {
        object {
            val name = "마이서울"
            val meetingTitle = "북촌조향사의집"
            val time = "1일 전"
            val content = "너무 좋은 투어를!!! 감사히 잘 다녀왔습니다\nmerci~~~"
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp)
    ) {
        item {
            Text(
                text = "받은 후기 83",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SuitFontFamily,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(reviews) { review ->
            Row(modifier = Modifier.padding(bottom = 24.dp)) {
                // Profile image
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6C60FD))
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = review.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SuitFontFamily,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${review.meetingTitle} · ${review.time}",
                            fontSize = 12.sp,
                            fontFamily = SuitFontFamily,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = stringResource(id = R.string.profile_rating),
                                tint = Color(0xFF6C60FD),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = review.content,
                        fontSize = 14.sp,
                        fontFamily = SuitFontFamily,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingTabContent(
    viewModel: ProfileViewModel,
    navController: NavController
) {
    // ViewModel 상태 구독
    val meetings by viewModel.myMeetings.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val totalMeetings by viewModel.totalMeetings.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                
                // 검색바 (활성화)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    placeholder = {
                        Text("만남 검색", color = Color.Gray, fontSize = 14.sp, fontFamily = SuitFontFamily)
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "검색",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFEEEEEE),
                        focusedBorderColor = Color(0xFFEEEEEE),
                        unfocusedContainerColor = Color(0xFFF7F7F7),
                        focusedContainerColor = Color(0xFFF7F7F7)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 동적으로 만남 개수 표시
                Text(
                    text = "${totalMeetings}개의 만남",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SuitFontFamily,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        items(meetings) { meeting ->
                var showMenu by remember { mutableStateOf(false) }
                var showDeleteDialog by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(Screen.MeetingDetail.createRoute(meeting.id))
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                    ) {
                        // 실제 이미지 표시 (이미지가 있으면)
                        if (meeting.imageUrls.isNotEmpty()) {
                            coil.compose.AsyncImage(
                                model = meeting.imageUrls.first(),
                                contentDescription = meeting.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // 더보기 메뉴 아이콘 (왼쪽 상단)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .clickable { showMenu = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "더보기",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("수정", fontFamily = SuitFontFamily) },
                                    onClick = {
                                        showMenu = false
                                        navController.navigate(Screen.AddMeeting.createRoute(meeting.id))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "수정"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("삭제", fontFamily = SuitFontFamily, color = Color.Red) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "삭제",
                                            tint = Color.Red
                                        )
                                    }
                                )
                            }
                        }

                        // 찜 아이콘 (오른쪽 상단)
                        Icon(
                            painter = painterResource(id = R.drawable.ic_heart),
                            contentDescription = "찜하기",
                            tint = if (meeting.isFavorited) Color.Red else Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(24.dp)
                        )

                        // 태그 및 상태 표시
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 만남 상태 배지 (최우선 표시)
                            when (meeting.status) {
                                "CLOSED" -> {
                                    Badge(containerColor = Color(0xFFFF9800)) {
                                        Text(
                                            stringResource(id = R.string.meeting_status_closed),
                                            color = Color.White,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                "COMPLETED" -> {
                                    Badge(containerColor = Color(0xFF4CAF50)) {
                                        Text(
                                            stringResource(id = R.string.meeting_status_completed),
                                            color = Color.White,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                "OPEN" -> {
                                    // 정원 마감 체크
                                    if (meeting.maxMembers != null && meeting.currentMembers != null &&
                                        meeting.currentMembers >= meeting.maxMembers) {
                                        Badge(containerColor = Color.Red) {
                                            Text(
                                                stringResource(id = R.string.meeting_status_full),
                                                color = Color.White,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // 기존 태그들 (최대 2개만 표시하여 공간 확보)
                            meeting.tags.take(2).forEach { tag ->
                                Badge(
                                    containerColor = when {
                                        tag.contains("혼잡") -> Color(0xFFFF6B6B)
                                        else -> Color(0xFF6C60FD)
                                    }
                                ) {
                                    Text(
                                        tag.removePrefix("#"),
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = meeting.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SuitFontFamily,
                        color = Color.Black,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = meeting.time,
                        fontSize = 12.sp,
                        fontFamily = SuitFontFamily,
                        color = Color.Gray
                    )
                    // 정원 정보 및 가격/평점 행
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = meeting.price,
                                fontSize = 12.sp,
                                fontFamily = SuitFontFamily,
                                color = Color.Gray
                            )
                            // 정원 정보 (있는 경우)
                            if (meeting.maxMembers != null && meeting.currentMembers != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(
                                        id = R.string.meeting_capacity_format,
                                        meeting.currentMembers,
                                        meeting.maxMembers
                                    ),
                                    fontSize = 11.sp,
                                    fontFamily = SuitFontFamily,
                                    color = if (meeting.currentMembers >= meeting.maxMembers)
                                        Color.Red else Color(0xFF6C60FD),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = stringResource(id = R.string.profile_rating),
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("%.1f", meeting.ratingAvg),
                                fontSize = 12.sp,
                                fontFamily = SuitFontFamily,
                                color = Color.Gray
                            )
                        }
                    }

                    // 삭제 확인 다이얼로그
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = {
                                Text(
                                    "만남 삭제",
                                    fontFamily = SuitFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Text(
                                    "정말로 이 만남을 삭제하시겠습니까?\n삭제된 만남은 복구할 수 없습니다.",
                                    fontFamily = SuitFontFamily
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showDeleteDialog = false
                                        viewModel.deleteMeeting(meeting.id)
                                    }
                                ) {
                                    Text("삭제", fontFamily = SuitFontFamily, color = Color.Red)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("취소", fontFamily = SuitFontFamily)
                                }
                            }
                        )
                    }
                }
            }
        }

        // 로딩 상태
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF6C60FD)
            )
        }

        // 빈 상태 (만남이 없을 때)
        if (!isLoading && meetings.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "만남 없음",
                    tint = Color.LightGray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (searchQuery.isNotBlank()) "검색 결과가 없습니다" else "아직 만남이 없습니다",
                    fontSize = 16.sp,
                    fontFamily = SuitFontFamily,
                    color = Color.Gray
                )
                if (searchQuery.isBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "첫 만남을 만들어보세요!",
                        fontSize = 14.sp,
                        fontFamily = SuitFontFamily,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}
