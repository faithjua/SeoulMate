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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.project.seoulmate.R
import com.project.seoulmate.data.model.Meeting
import com.project.seoulmate.ui.components.BottomNavigationBar
import com.project.seoulmate.ui.navigation.Screen
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
    // 하단 네비게이션 선택 상태 (찜 화면이므로 1)
    val selectedBottomItem = 1

    // 알림 개수를 저장하는 상태 변수 (현재 사용 안 함)
    // var unreadAlarmCount by remember { mutableStateOf(2) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedBottomItem,
                onItemSelected = { index ->
                    when (index) {
                        0 -> navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                        2 -> navController.navigate(Screen.AddMeeting.route)
                        // TODO: Handle other tabs when implemented
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
                    text = "찜",
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
                            contentDescription = "번역",
                            contentScale = ContentScale.None
                        )
                    }

                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(
                            onClick = { navController.navigate(Screen.Notifications.route) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "알림",
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
                FilterChipItem(text = "지역·명소", options = listOf("전체", "서울", "부산", "제주"))
                FilterChipItem(text = "메이트 선호", options = listOf("전체", "동성", "이성", "무관"))
                FilterChipItem(text = "혼잡도", options = listOf("전체", "여유", "보통", "혼잡"))
                
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
                            contentDescription = "필터 설정",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Count
            Text(
                text = "총 ${meetings.size}개",
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
    options: List<String> = emptyList() // 드롭다운에 보여줄 항목들
) {
    // 드롭다운 메뉴가 열려있는지 여부를 저장하는 상태
    var expanded by remember { mutableStateOf(false) }
    // 현재 선택된 텍스트를 저장하는 상태 (기본값은 처음에 전달받은 text)
    var selectedText by remember { mutableStateOf(text) }

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
                    text = selectedText, // 변경되는 텍스트 적용
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
        
        // 드롭다운 메뉴 레고 조립!
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }, // 바깥을 누르면 닫힘
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedText = option // 선택한 항목으로 글자 변경
                        expanded = false // 선택 후 메뉴 닫기
                    }
                )
            }
        }
    }
}

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
            Image(
                painter = painterResource(id = meeting.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Heart Icon (Top Right)
            IconButton(
                onClick = { onRemoveFavorite() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "찜 취소",
                    tint = Color(0xFFFF6B6B), // Red-pinkish color
                    modifier = Modifier.size(28.dp)
                )
            }

            // Tags (Bottom Left)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                meeting.tags.forEach { tag ->
                    val isCrowded = tag == "혼잡"
                    val isFree = tag == "여유"
                    val isUnknown = tag == "정보 없음"
                    Surface(
                        color = when {
                            isCrowded -> Color(0xFFFF6B6B) // Orange/Red
                            isFree -> Color.White // White with dark text
                            isUnknown -> Color(0xFF9E9E9E) // 회색
                            else -> Color(0xFF6C60FD) // Purple
                        },
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = tag,
                            color = if (isFree) Color(0xFF6C60FD) else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
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
                text = "예상 ${meeting.price}",
                color = Color(0xFF888888),
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "평점",
                tint = Color(0xFF888888),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = meeting.rating,
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
