package com.project.seoulmate.ui.screens.notification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.project.seoulmate.R
import com.project.seoulmate.ui.components.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavHostController
) {
    var selectedBottomItem by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "알림",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "뒤로가기",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedBottomItem,
                onItemSelected = { index ->
                    when (index) {
                        0 -> navController.navigate(com.project.seoulmate.ui.navigation.Screen.Home.route) {
                            popUpTo(com.project.seoulmate.ui.navigation.Screen.Home.route) { inclusive = true }
                        }
                        1 -> navController.navigate(com.project.seoulmate.ui.navigation.Screen.Wishlist.route) {
                            popUpTo(com.project.seoulmate.ui.navigation.Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        2 -> navController.navigate(com.project.seoulmate.ui.navigation.Screen.AddMeeting.route)
                        else -> selectedBottomItem = index
                    }
                }
            )
        },
        containerColor = Color(0xFFF9F9F9) // 아주 연한 회색 배경
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 알림 리스트
            NotificationItem(
                profileImage = { ProfileMaskIcon() },
                meetingImageRes = R.drawable.img_recommend_1,
                username = "썰메야",
                timeAgo = "1분 전",
                message = "예약 신청을 보내왔습니다.",
                showButtons = true
            )
            
            NotificationItem(
                profileImage = { ProfileMaskIcon() },
                meetingImageRes = R.drawable.img_recommend_2,
                username = "썰메야",
                timeAgo = "1분 전",
                message = "안녕하세요 ^^",
                showButtons = false
            )
        }
    }
}

@Composable
fun ProfileMaskIcon() {
    Image(
        painter = painterResource(id = R.drawable.ic_notification_profile),
        contentDescription = "프로필 이미지",
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
    )
}

@Composable
fun NotificationItem(
    profileImage: @Composable () -> Unit,
    meetingImageRes: Int,
    username: String,
    timeAgo: String,
    message: String,
    showButtons: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        profileImage()
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 사각형 사진
        Image(
            painter = painterResource(id = meetingImageRes),
            contentDescription = "만남 이미지",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = username,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = timeAgo,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        
        if (showButtons) {
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C60FD)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .widthIn(min = 52.dp)
                ) {
                    Text("승인", fontSize = 13.sp, color = Color.White)
                }
                
                OutlinedButton(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC0C0C0)),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .widthIn(min = 52.dp)
                ) {
                    Text("무시", fontSize = 13.sp, color = Color(0xFFD0D0D0))
                }
            }
        }
    }
}
