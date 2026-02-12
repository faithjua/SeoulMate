package com.project.seoulmate.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.seoulmate.R

data class Category(
    val name: String,
    @DrawableRes val icon: Int
)

@Composable
fun CategoryGrid(
    modifier: Modifier = Modifier,
    onCategoryClick: (Category) -> Unit = {}
) {
    val categories = listOf(
        Category("관광", R.drawable.ic_tourism),
        Category("K-팝", R.drawable.ic_kpop),
        Category("K-뷰티", R.drawable.ic_kbeauty),
        Category("쇼핑", R.drawable.ic_shopping),
        Category("한식", R.drawable.ic_food),
        Category("카페", R.drawable.ic_cafe),
        Category("교통 가이드", R.drawable.ic_transport),
        Category("숙소/지역", R.drawable.ic_accommodation),
        Category("클래스", R.drawable.ic_class),
        Category("커뮤니티", R.drawable.ic_community),
        Category("전시/공연", R.drawable.ic_exhibition),
        Category("안전/생활", R.drawable.ic_safety)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .height(220.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3F3F3)
        )
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = category.icon),
            contentDescription = category.name,
            modifier = Modifier
                .shadow(
                    elevation = 6.8.dp,
                    spotColor = Color(0xFFFEC2F2),
                    ambientColor = Color(0xFFFEC2F2)
                )
                .size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            color = Color(0xFF6C60FD)
        )
    }
}

@Preview
@Composable
fun CategoryGridPreview() {
    CategoryGrid()
}