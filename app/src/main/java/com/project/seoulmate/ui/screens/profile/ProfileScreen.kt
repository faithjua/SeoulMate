package com.project.seoulmate.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.project.seoulmate.R
import com.project.seoulmate.config.AppConfig
import com.project.seoulmate.ui.components.BottomNavigationBar
import com.project.seoulmate.ui.components.SuitFontFamily
import com.project.seoulmate.ui.navigation.Screen
import com.project.seoulmate.ui.theme.SeoulMatePrimary
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(2) } // default to stringResource(id = R.string.profile_tab_info) (Index 2)
    var selectedBottomItem by remember { mutableStateOf(3) } // Profile is index 3
    var showMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ViewModel 상태
    val profileImageUrl by viewModel.profileImageUrl.collectAsStateWithLifecycle()
    val uploadingImage by viewModel.uploadingImage.collectAsStateWithLifecycle()
    val bio by viewModel.bio.collectAsStateWithLifecycle()
    val updatingBio by viewModel.updatingBio.collectAsStateWithLifecycle()

    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadProfileImage(it)
        }
    }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            android.widget.Toast.makeText(
                context,
                "이미지 접근 권한이 필요합니다",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 자기소개 편집 다이얼로그 상태
    var showBioEditDialog by remember { mutableStateOf(false) }

    // 프로필 로드
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

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

    // 자기소개 편집 다이얼로그
    if (showBioEditDialog) {
        BioEditDialog(
            currentBio = bio,
            isUpdating = updatingBio,
            onDismiss = { showBioEditDialog = false },
            onSave = { newBio ->
                viewModel.updateBio(
                    newBio = newBio,
                    onSuccess = {
                        showBioEditDialog = false
                    },
                    onError = { error ->
                        // TODO: 에러 표시 (Toast 또는 Snackbar)
                    }
                )
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
                        2 -> navController.navigate(Screen.AddMeeting.createRoute())
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
            ProfileHeader(
                profileImageUrl = profileImageUrl,
                uploadingImage = uploadingImage,
                bio = bio,
                onEditProfileImageClick = {
                    // Android 버전에 따라 적절한 권한 요청
                    val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    }

                    when {
                        androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            permission
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                            // 권한 있음 - 바로 이미지 선택
                            imagePickerLauncher.launch("image/*")
                        }
                        else -> {
                            // 권한 없음 - 권한 요청
                            permissionLauncher.launch(permission)
                        }
                    }
                },
                onEditBioClick = {
                    showBioEditDialog = true
                }
            )

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
                    1 -> ReviewTabContent(viewModel = viewModel)
                    2 -> BadgeTabContent(viewModel = viewModel, navController = navController)
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
fun ProfileHeader(
    profileImageUrl: String? = null,
    uploadingImage: Boolean = false,
    bio: String = "",
    onEditProfileImageClick: () -> Unit = {},
    onEditBioClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Box(
                modifier = Modifier.size(72.dp)
            ) {
                if (profileImageUrl != null) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        placeholder = painterResource(id = R.drawable.img_default_profile),
                        error = painterResource(id = R.drawable.img_default_profile)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_default_profile),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                // 업로드 중 로딩 표시
                if (uploadingImage) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        color = Color(0xFF6C60FD),
                        strokeWidth = 3.dp
                    )
                }

                // 연필 버튼 (프로필 이미지 편집)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.BottomEnd)
                        .clickable { onEditProfileImageClick() },
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
            text = bio.ifEmpty { "자기소개를 입력해주세요" },
            fontSize = 14.sp,
            fontFamily = SuitFontFamily,
            color = if (bio.isEmpty()) Color.Gray else Color.Black,
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
            onClick = onEditBioClick,
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
fun BadgeTabContent(
    viewModel: ProfileViewModel,
    navController: NavController
) {
    // API에서 가져온 배지 데이터
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    val loadingBadges by viewModel.loadingBadges.collectAsStateWithLifecycle()

    // 12개 배지 정의 (이름, 아이콘 리소스)
    // categoryCode와 매핑되는 drawable 리소스
    val badgeList = remember {
        listOf(
            BadgeData("관광", R.drawable.badge_tour),
            BadgeData("K-팝", R.drawable.badge_kpop),
            BadgeData("K-뷰티", R.drawable.badge_kbeauty),
            BadgeData("쇼핑", R.drawable.badge_shopping),
            BadgeData("한식", R.drawable.badge_kfood),
            BadgeData("카페", R.drawable.badge_cafe),
            BadgeData("교통가이드", R.drawable.badge_transport),
            BadgeData("클래스", R.drawable.badge_class),
            BadgeData("전시·스타일", R.drawable.badge_style),
            BadgeData("커뮤니티", R.drawable.badge_community),
            BadgeData("숙소·지역", R.drawable.badge_region),
            BadgeData("안전", R.drawable.badge_safety)
        )
    }

    // API에서 받은 배지의 categoryCode 집합 (보유한 배지만 활성화)
    val earnedBadgeCodes = remember(badges) {
        badges.map { it.categoryCode }.toSet()
    }

    if (loadingBadges) {
        // 로딩 중 표시
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SeoulMatePrimary)
        }
    } else {
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
                // API에서 받은 배지 데이터에 이 배지가 있는지 확인
                val badgeData = badges.find { it.categoryCode == badge.name }
                val isEarned = badgeData != null

                BadgeGridItem(
                    badge = badge,
                    isSelected = isEarned,
                    count = badgeData?.count ?: 0,
                    onClick = {
                        // 배지는 클릭해도 상태가 바뀌지 않음 (읽기 전용)
                    }
                )
            }
        }
    }
}

data class BadgeData(
    val name: String,
    val iconRes: Int
)

@Composable
fun BadgeGridItem(
    badge: BadgeData,
    isSelected: Boolean,
    count: Int = 0,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth(0.9f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = badge.iconRes),
                contentDescription = badge.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                // 비활성화 상태일 경우 채도를 0으로 바꾸어 흑백 처리 + 투명도 조정
                colorFilter = if (isSelected) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                alpha = if (isSelected) 1f else 0.45f
            )

            // 배지 획득 횟수 표시 (획득한 경우에만)
            if (isSelected && count > 0) {
                Text(
                    text = "×$count",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SuitFontFamily,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            color = SeoulMatePrimary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = badge.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = SuitFontFamily,
            color = if (isSelected) Color(0xFF424242) else Color(0xFF9E9E9E),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ReviewTabContent(viewModel: ProfileViewModel) {
    // ViewModel 상태 구독
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val loadingReviews by viewModel.loadingReviews.collectAsStateWithLifecycle()
    val totalReviews by viewModel.totalReviews.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp)
        ) {
            item {
                Text(
                    text = "받은 후기 $totalReviews",
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
                    if (review.authorProfileImage != null) {
                        AsyncImage(
                            model = review.authorProfileImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            placeholder = painterResource(id = R.drawable.img_default_profile),
                            error = painterResource(id = R.drawable.img_default_profile)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6C60FD))
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = review.authorNickname,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SuitFontFamily,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${review.meetupContextTitle} · ${formatReviewTime(review.createdAt)}",
                                fontSize = 12.sp,
                                fontFamily = SuitFontFamily,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            repeat(5) { index ->
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = stringResource(id = R.string.profile_rating),
                                    tint = if (index < review.rating) Color(0xFF6C60FD) else Color(0xFFE0E0E0),
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

        // 로딩 상태
        if (loadingReviews) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = SeoulMatePrimary
            )
        }

        // 빈 상태 (리뷰가 없을 때)
        if (!loadingReviews && reviews.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "리뷰 없음",
                    tint = Color.LightGray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "아직 받은 후기가 없습니다",
                    fontSize = 16.sp,
                    fontFamily = SuitFontFamily,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * ISO 8601 시간 문자열을 상대 시간으로 변환
 * 예: "2026-05-12T22:46:52.063Z" -> "1일 전"
 */
private fun formatReviewTime(isoTime: String): String {
    return try {
        // 간단한 구현 - 실제로는 더 정교한 시간 계산 필요
        // TODO: 실제 시간 차이 계산 로직 추가
        val now = System.currentTimeMillis()
        val timePattern = """(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})""".toRegex()
        val match = timePattern.find(isoTime)

        if (match != null) {
            val (year, month, day, hour, minute, second) = match.destructured
            val reviewDate = java.util.Calendar.getInstance().apply {
                set(year.toInt(), month.toInt() - 1, day.toInt(), hour.toInt(), minute.toInt(), second.toInt())
            }.timeInMillis

            val diffMillis = now - reviewDate
            val diffDays = diffMillis / (1000 * 60 * 60 * 24)
            val diffHours = diffMillis / (1000 * 60 * 60)
            val diffMinutes = diffMillis / (1000 * 60)

            when {
                diffDays > 0 -> "${diffDays}일 전"
                diffHours > 0 -> "${diffHours}시간 전"
                diffMinutes > 0 -> "${diffMinutes}분 전"
                else -> "방금 전"
            }
        } else {
            isoTime.substring(0, 10) // 날짜만 표시
        }
    } catch (e: Exception) {
        isoTime.substring(0, 10) // 날짜만 표시
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

/**
 * 자기소개 편집 다이얼로그
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioEditDialog(
    currentBio: String,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var bioText by remember { mutableStateOf(currentBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "자기소개 수정",
                fontFamily = SuitFontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = bioText,
                    onValueChange = { bioText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = {
                        Text(
                            "자기소개를 입력해주세요",
                            fontFamily = SuitFontFamily,
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFEEEEEE),
                        focusedBorderColor = Color(0xFF6C60FD)
                    ),
                    maxLines = 10,
                    enabled = !isUpdating
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(bioText) },
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFF6C60FD),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "저장",
                        fontFamily = SuitFontFamily,
                        color = Color(0xFF6C60FD)
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUpdating
            ) {
                Text(
                    "취소",
                    fontFamily = SuitFontFamily
                )
            }
        }
    )
}
