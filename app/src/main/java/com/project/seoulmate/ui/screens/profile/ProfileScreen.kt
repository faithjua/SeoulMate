package com.project.seoulmate.ui.screens.profile

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
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.project.seoulmate.R
import com.project.seoulmate.ui.components.BottomNavigationBar
import com.project.seoulmate.ui.components.SuitFontFamily
import com.project.seoulmate.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf(2) } // default to "정보" (Index 2)
    var selectedBottomItem by remember { mutableStateOf(4) } // Profile is index 4

    Scaffold(
        containerColor = Color.White,
        topBar = {
            ProfileTopBar(
                onBackClick = { navController.popBackStack() },
                onMenuClick = { /* TODO */ }
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

            Spacer(modifier = Modifier.height(16.dp))

            // 탭 (만남, 리뷰, 정보)
            val tabs = listOf("만남", "리뷰", "정보")
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
                                color = if (selectedTab == index) Color(0xFF6C60FD) else Color.Gray
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
                    0 -> MeetingTabContent()
                    1 -> ReviewTabContent()
                    2 -> InfoTabContent()
                }
            }
        }
    }
}

@Composable
fun ProfileTopBar(onBackClick: () -> Unit, onMenuClick: () -> Unit) {
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
            contentDescription = "뒤로가기",
            modifier = Modifier
                .size(32.dp)
                .clickable { onBackClick() },
            tint = Color.Black
        )
        Icon(
            imageVector = Icons.Default.MoreHoriz,
            contentDescription = "메뉴",
            modifier = Modifier
                .size(32.dp)
                .clickable { onMenuClick() },
            tint = Color.Black
        )
    }
}

@Composable
fun ProfileHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image (Placeholder for now)
            Box(
                modifier = Modifier.size(72.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF6C60FD)),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder for actual image
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
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
                        contentDescription = "수정",
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
                        painter = painterResource(id = R.drawable.ic_add), // TODO: Change to verify badge
                        contentDescription = "인증됨",
                        tint = Color(0xFF6C60FD),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "별점",
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "인스타 @imseoul 워킹맘\n관광학부 전공으로 개인 투어 맛집입니다 허허\n\n영어, 프랑스어, 한국어 가능합니다^^",
            fontSize = 14.sp,
            fontFamily = SuitFontFamily,
            color = Color.Black,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "간략히 보기",
            fontSize = 12.sp,
            fontFamily = SuitFontFamily,
            color = Color.Gray,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { /* TODO */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
        ) {
            Text(
                text = "수정하기",
                fontSize = 16.sp,
                fontFamily = SuitFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun InfoTabContent() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Text(
            text = "최근 3일 이내 활동함",
            fontSize = 14.sp,
            fontFamily = SuitFontFamily,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "가입한 날짜",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = SuitFontFamily,
            color = Color.Black
        )
        Text(
            text = "2026.02.20.",
            fontSize = 14.sp,
            fontFamily = SuitFontFamily,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "인증 내역",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = SuitFontFamily,
            color = Color.Black
        )
        Text(
            text = "이메일 인증 완료",
            fontSize = 14.sp,
            fontFamily = SuitFontFamily,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "이전 사용자 이름",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = SuitFontFamily,
            color = Color.Black
        )
        Text(
            text = "소울이님은 사용자 이름을 2회 변경했습니다.",
            fontSize = 14.sp,
            fontFamily = SuitFontFamily,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "FAQ · 문의하기 ·\nCopyright © 2026 SEOULMATE. All Rights Reserved.",
            fontSize = 12.sp,
            fontFamily = SuitFontFamily,
            color = Color.LightGray,
            lineHeight = 18.sp
        )
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
                                contentDescription = "별점",
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
fun MeetingTabContent() {
    // Dummy Data
    val meetings = List(4) {
        object {
            val title = "창덕궁 탐방 및 맛집"
            val time = "28일 오후 7-9시"
            val price = "예상 ₩20,000"
            val rating = "5.0"
        }
    }

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
                
                // 검색바 (간단하게 구현)
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
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

                Text(
                    text = "55개의 만남",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SuitFontFamily,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        items(meetings) { meeting ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                    ) {
                        // Image placeholder
                        Icon(
                            painter = painterResource(id = R.drawable.ic_heart),
                            contentDescription = "찜하기",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(24.dp)
                        )
                        // Tags placeholder
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Badge(containerColor = Color(0xFFFF6B6B)) { Text("혼잡", color = Color.White) }
                            Badge(containerColor = Color(0xFF6C60FD)) { Text("관광", color = Color.White) }
                            Badge(containerColor = Color(0xFF6C60FD)) { Text("한식", color = Color.White) }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = meeting.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SuitFontFamily,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = meeting.time,
                        fontSize = 12.sp,
                        fontFamily = SuitFontFamily,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = meeting.price,
                            fontSize = 12.sp,
                            fontFamily = SuitFontFamily,
                            color = Color.Gray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "별점",
                                tint = Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = meeting.rating,
                                fontSize = 12.sp,
                                fontFamily = SuitFontFamily,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
}
