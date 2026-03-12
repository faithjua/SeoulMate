package com.project.seoulmate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                CategoryIconItem(
                    category = category,
                    isSelected = category == selectedCategory,
                    onClick = { onCategoryClick(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 스크롤 진행도 표시 바 (커스텀)
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(1.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(2.dp)
                    .background(Color(0xFF6C60FD), RoundedCornerShape(1.dp))
            )
        }
    }
}

@Composable
fun CategoryIconItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF6C60FD) else Color(0xFFEAEAEA)
    val backgroundColor = if (isSelected) Color(0xFF6C60FD) else Color.White
    val textColor = if (isSelected) Color.Black else Color(0xFF888888)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(1.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (category.isAllMenu) {
                Icon(
                    imageVector = Icons.Rounded.GridView,
                    contentDescription = category.name,
                    tint = if (isSelected) Color.White else Color(0xFF6C60FD),
                    modifier = Modifier.size(28.dp)
                )
            } else if (category.iconRes != null) {
                Image(
                    painter = painterResource(id = category.iconRes),
                    contentDescription = category.name,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = category.name,
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
