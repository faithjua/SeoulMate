package com.project.seoulmate.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.project.seoulmate.ui.theme.SeoulMateBackground

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



    // Calculate rows needed (12 items / 4 columns = 3 rows)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp) // Outer padding
            .background(color = SeoulMateBackground, shape = RoundedCornerShape(16.dp)) // Grey background
            .padding(horizontal = 24.dp, vertical = 16.dp), // Inner padding
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val chunkedCategories = categories.chunked(4)
        chunkedCategories.forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowCategories.forEach { category ->
                    CategoryItem(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty spots if last row is incomplete (though here it is 12 items exactly)
                repeat(4 - rowCategories.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon with styling
        Image(
            painter = painterResource(id = category.icon),
            contentDescription = category.name,
            modifier = Modifier
                .size(48.dp) // Increased size based on visual
                // Shadow removed for cleaner look or adjusted if needed
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = Color(0xFF6C60FD) // Keep the branding color
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryGridPreview() {
    CategoryGrid()
}