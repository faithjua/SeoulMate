package com.project.seoulmate.ui.components

import com.project.seoulmate.config.AppConfig

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.seoulmate.R

// SUIT 폰트 정의
val SuitFontFamily = FontFamily(
    Font(R.font.suit_regular, FontWeight.Normal),
    Font(R.font.suit_regular, FontWeight.Medium),
    Font(R.font.suit_bold, FontWeight.Bold)
)

// 하단바 색상
object BottomNavColors {
    val Selected = Color(0xFF6C60FD)
    val Unselected = Color(0xFF929292)
}

// 하단바 아이템 데이터
sealed class BottomNavItem(
    val title: String,
    @DrawableRes val icon: Int
) {
    object Home : BottomNavItem("홈", R.drawable.ic_home)
    object Favorite : BottomNavItem("찜", R.drawable.ic_heart)
    object Add : BottomNavItem("등록", R.drawable.ic_add)
    object Profile : BottomNavItem("프로필", R.drawable.ic_profile)
}

@Composable
fun BottomNavigationBar(
    selectedItem: Int = 0,
    onItemSelected: (Int) -> Unit = {}
) {
    val items = buildList {
        add(BottomNavItem.Home)     // index 0
        add(BottomNavItem.Favorite) // index 1
        add(BottomNavItem.Add)      // index 2
        if (!AppConfig.IS_PRODUCTION) {
            add(BottomNavItem.Profile)  // index 3 (개발 모드만)
        }
    }

    Surface(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x0F000000),
                ambientColor = Color(0x0F000000)
            )
            .fillMaxWidth()
            .height(63.dp),
        color = Color(0xFFFFFFFF)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                BottomNavItem(
                    item = item,
                    isSelected = selectedItem == index,
                    onClick = { onItemSelected(index) }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .padding(horizontal = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 아이콘
        Icon(
            painter = painterResource(id = item.icon),
            contentDescription = item.title,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) {
                BottomNavColors.Selected
            } else {
                BottomNavColors.Unselected
            }
        )

        // 텍스트
        Text(
            text = item.title,
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = SuitFontFamily,
                fontWeight = FontWeight(500),
                color = if (isSelected) {
                    BottomNavColors.Selected
                } else {
                    BottomNavColors.Unselected
                },
                textAlign = TextAlign.Center
            ),
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    Column {
        BottomNavigationBar(selectedItem = 0)
        Spacer(modifier = Modifier.height(16.dp))
        BottomNavigationBar(selectedItem = 2)
    }
}