package com.project.seoulmate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.seoulmate.data.model.Category
import com.project.seoulmate.ui.util.displayCategoryName

/**
 * 카테고리 가로 스크롤 섹션.
 *
 * @param categories ViewModel에서 내려보낸 카테고리 목록
 * @param selectedCategory 현재 선택된 카테고리 (ViewModel이 관리)
 * @param onCategoryClick 카테고리 클릭 시 ViewModel 함수 호출
 */
@Composable
fun CategorySection(
    modifier: Modifier = Modifier,
    categories: List<Category> = emptyList(),
    selectedCategory: Category? = null,
    onCategoryClick: (Category) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val scrollState = rememberScrollState()
        val scrollProgress = if (scrollState.maxValue > 0) {
            scrollState.value.toFloat() / scrollState.maxValue.toFloat()
        } else {
            0f
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.width(24.dp))
            categories.forEachIndexed { index, category ->
                CategoryIconItem(
                    category = category,
                    isSelected = category == selectedCategory,
                    onClick = { onCategoryClick(category) }
                )
                if (index < categories.size - 1) {
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
            Spacer(modifier = Modifier.width(24.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 스크롤 진행도 표시 바 (커스텀)
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(1.dp))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val indicatorWidth = maxWidth * 0.3f
                val maxOffset = maxWidth - indicatorWidth
                val currentOffset = maxOffset * scrollProgress

                Box(
                    modifier = Modifier
                        .offset(x = currentOffset)
                        .width(indicatorWidth)
                        .fillMaxHeight()
                        .background(Color(0xFF6C60FD), RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

@Composable
fun CategoryIconItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF6C60FD) else Color(0xFFF6F6F6)
    val iconTint = if (isSelected) Color.White else Color(0xFF6C60FD)
    val textColor = if (isSelected) Color.Black else Color(0xFF888888)
    val displayName = displayCategoryName(category.name)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = backgroundColor,
            shadowElevation = 2.dp // 피그마와 비슷한 입체감 추가
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (category.isAllMenu) {
                    Icon(
                        imageVector = Icons.Rounded.GridView,
                        contentDescription = displayName,
                        tint = iconTint,
                        modifier = Modifier.size(28.dp)
                    )
                } else if (category.iconRes != null) {
                    Icon(
                        painter = painterResource(id = category.iconRes),
                        contentDescription = displayName,
                        tint = iconTint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = displayName,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategorySectionPreview() {
    CategorySection()
}