package com.project.seoulmate.ui.screens.notification

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.project.seoulmate.R
import com.project.seoulmate.data.remote.ApplicationResponse
import com.project.seoulmate.data.remote.NotificationResponse
import com.project.seoulmate.ui.components.BottomNavigationBar
import com.project.seoulmate.ui.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavHostController,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    var selectedBottomItem by remember { mutableStateOf(0) }
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 액션 결과 처리. ViewModel은 resId를 emit하므로 여기서 현재 locale로 변환.
    LaunchedEffect(viewModel.actionResult) {
        viewModel.actionResult.collectLatest { result ->
            when (result) {
                is NotificationViewModel.ActionResult.Success -> {
                    Toast.makeText(context, context.getString(result.messageRes), Toast.LENGTH_SHORT).show()
                    viewModel.clearActionResult()
                }
                is NotificationViewModel.ActionResult.Error -> {
                    val msg = result.dynamicMessage ?: context.getString(result.messageRes)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearActionResult()
                }
                null -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.notification_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = stringResource(id = R.string.meeting_back),
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
                        0 -> navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                        1 -> navController.navigate(Screen.Wishlist.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        2 -> navController.navigate(Screen.AddMeeting.createRoute())
                        else -> selectedBottomItem = index
                    }
                }
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && notifications.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF6C60FD)
                )
            } else if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.notification_empty),
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { notification ->
                        when (notification) {
                            is UnifiedNotification.ApplicationNotification -> {
                                ApplicationNotificationItem(
                                    application = notification.application,
                                    onApprove = { viewModel.approveApplication(it.id) },
                                    onReject = { viewModel.rejectApplication(it.id) },
                                    onMeetingClick = {
                                        navController.navigate(
                                            Screen.MeetingDetail.createRoute(it.meetupId.toString())
                                        )
                                    }
                                )
                            }
                            is UnifiedNotification.StatusNotification -> {
                                StatusNotificationItem(
                                    notification = notification.notification,
                                    onNotificationClick = {
                                        viewModel.markNotificationAsRead(it.id)
                                        // 만남 상세로 이동
                                        it.metadata?.meetupId?.let { meetupId ->
                                            navController.navigate(
                                                Screen.MeetingDetail.createRoute(meetupId.toString())
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMaskIcon() {
    Image(
        painter = painterResource(id = R.drawable.ic_notification_profile),
        contentDescription = stringResource(id = R.string.meeting_profile_image),
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
    )
}

/**
 * 메이트 신청 알림 아이템 (호스트가 받은 신청)
 */
@Composable
fun ApplicationNotificationItem(
    application: ApplicationResponse,
    onApprove: (ApplicationResponse) -> Unit,
    onReject: (ApplicationResponse) -> Unit,
    onMeetingClick: (ApplicationResponse) -> Unit
) {
    val recentFallback = stringResource(id = R.string.notification_time_recent)
    val timeAgo = remember(application.createdAt, recentFallback) {
        calculateTimeAgo(application.createdAt, recentFallback)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onMeetingClick(application) }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 신청자 프로필 이미지
        if (application.applicantProfileImage != null) {
            AsyncImage(
                model = application.applicantProfileImage,
                contentDescription = stringResource(id = R.string.meeting_profile_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        } else {
            ProfileMaskIcon()
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 만남 썸네일 이미지
        if (application.meetupThumbnailUrl != null) {
            AsyncImage(
                model = application.meetupThumbnailUrl,
                contentDescription = stringResource(id = R.string.notification_meeting_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_tourism),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = application.applicantNickname,
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
                text = application.message ?: stringResource(id = R.string.notification_reservation_request),
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2
            )
        }

        // PENDING 상태인 경우에만 승인/거절 버튼 표시
        if (application.status == "PENDING") {
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { onApprove(application) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C60FD)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        stringResource(id = R.string.notification_approve),
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = { onReject(application) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC0C0C0)),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        stringResource(id = R.string.notification_ignore),
                        fontSize = 13.sp,
                        color = Color(0xFFD0D0D0)
                    )
                }
            }
        } else {
            // 이미 처리된 경우 상태 표시
            Spacer(modifier = Modifier.width(8.dp))
            val statusLabel = when (application.status) {
                "ACCEPTED" -> stringResource(id = R.string.notification_status_accepted)
                "REJECTED" -> stringResource(id = R.string.notification_status_rejected)
                else -> application.status
            }
            Text(
                text = statusLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = when (application.status) {
                    "ACCEPTED" -> Color(0xFF6C60FD)
                    "REJECTED" -> Color.Gray
                    else -> Color.Gray
                }
            )
        }
    }
}

/**
 * 승인/거절 상태 알림 아이템 (신청자가 받은 알림)
 */
@Composable
fun StatusNotificationItem(
    notification: NotificationResponse,
    onNotificationClick: (NotificationResponse) -> Unit
) {
    val recentFallback = stringResource(id = R.string.notification_time_recent)
    val timeAgo = remember(notification.createdAt, recentFallback) {
        calculateTimeAgo(notification.createdAt, recentFallback)
    }

    // 알림 타입에 따른 스타일 결정
    val icon: Int
    val iconColor: Color
    val statusText: String
    val statusColor: Color

    when (notification.type) {
        "APPLICATION_ACCEPTED" -> {
            icon = R.drawable.ic_check_circle
            iconColor = Color(0xFF6C60FD)
            statusText = stringResource(id = R.string.notification_status_accepted)
            statusColor = Color(0xFF6C60FD)
        }
        "APPLICATION_REJECTED" -> {
            icon = R.drawable.ic_cancel
            iconColor = Color.Gray
            statusText = stringResource(id = R.string.notification_status_rejected)
            statusColor = Color.Gray
        }
        "MEETUP_CLOSED" -> {
            icon = R.drawable.ic_notification
            iconColor = Color(0xFFFF6B6B)
            statusText = stringResource(id = R.string.notification_status_closed)
            statusColor = Color(0xFFFF6B6B)
        }
        "MEETUP_COMPLETED" -> {
            icon = R.drawable.ic_check_circle
            iconColor = Color(0xFF4CAF50)
            statusText = stringResource(id = R.string.notification_status_completed)
            statusColor = Color(0xFF4CAF50)
        }
        "MEETUP_REOPENED" -> {
            icon = R.drawable.ic_notification
            iconColor = Color(0xFF6C60FD)
            statusText = stringResource(id = R.string.notification_status_reopened)
            statusColor = Color(0xFF6C60FD)
        }
        else -> {
            icon = R.drawable.ic_notification
            iconColor = Color(0xFF6C60FD)
            statusText = stringResource(id = R.string.notification_status_default)
            statusColor = Color.Gray
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notification.isRead) Color.White else Color(0xFFF5F5FF))
            .clickable { onNotificationClick(notification) }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 상태 아이콘
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 만남 썸네일 (있는 경우)
        notification.metadata?.meetupThumbnailUrl?.let { thumbnailUrl ->
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = stringResource(id = R.string.common_meeting_thumbnail),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    fontSize = 15.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
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
                text = notification.message,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2
            )

            // 만남 제목 (있는 경우)
            notification.metadata?.meetupTitle?.let { title ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.notification_meeting_label, title),
                    fontSize = 13.sp,
                    color = Color(0xFF6C60FD),
                    maxLines = 1
                )
            }
        }

        // 상태 표시
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = statusText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = statusColor
        )
    }
}

/**
 * 시간 차이 계산 (간단 버전).
 * 파싱 실패 시 fallback 라벨은 호출부에서 stringResource로 주입.
 */
private fun calculateTimeAgo(createdAt: String, fallback: String): String {
    return try {
        // ISO 8601 형식에서 날짜/시간 추출 (예: "2026-05-11T13:26:12.345Z")
        val dateTimePart = createdAt.substringBefore(".")
        val parts = dateTimePart.split("T")
        if (parts.size >= 2) {
            val datePart = parts[0] // "2026-05-11"
            val timePart = parts[1].substring(0, 5) // "13:26"
            "$datePart $timePart"
        } else {
            fallback
        }
    } catch (e: Exception) {
        fallback
    }
}