package com.project.seoulmate.ui.components

import com.project.seoulmate.config.AppConfig

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.seoulmate.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onTranslateClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    hasUnreadNotifications: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_seoul_mate),
            contentDescription = stringResource(id = R.string.topbar_logo),
            modifier = Modifier
                .width(106.dp)
                .height(20.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 번역 아이콘은 항상 노출 (앱 로케일 토글)
            IconButton(onClick = onTranslateClick) {
                Image(
                    painter = painterResource(id = R.drawable.ic_translate),
                    contentDescription = stringResource(id = R.string.wishlist_translate),
                    contentScale = ContentScale.None
                )
            }

            if (!AppConfig.IS_PRODUCTION) {
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = stringResource(id = R.string.wishlist_notification),
                            modifier = Modifier.size(28.dp),
                            tint = Color.Black
                        )
                    }

                    // 읽지 않은 알림이 있을 때만 빨간 배지 표시
                    if (hasUnreadNotifications) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp, end = 6.dp)
                                .size(10.dp)
                                .background(color = Color(0xFFFF6B6B), shape = CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TopBarPreview() {
    TopBar()
}