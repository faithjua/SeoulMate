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

// @Composable annotation tells the compiler that this function is intended to convert data into UI.
@Composable
fun CategoryGrid(
    modifier: Modifier = Modifier,
    onCategoryClick: (Category) -> Unit = {}
) {
    // List of data items to display. We use a data class 'Category' to hold the name and icon resource.
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

    // Layout Choice Explanation:
    // Originally, we might use LazyVerticalGrid here. However, this CategoryGrid is placed inside
    // a Column in HomeScreen that is ALREADY scrollable (Modifier.verticalScroll).
    // Nesting a scrollable grid (LazyVerticalGrid) inside another scrollable container (Column)
    // causes a crash because the inner grid tries to expand infinitely.
    // FIX: We use a simple Column and Row combination because we have a small, fixed number of items (12).
    // This removes the scrolling capability from the grid itself, letting the parent HomeScreen handle scrolling.

    // Calculate rows needed (12 items / 4 columns = 3 rows)
    Column(
        modifier = modifier
            .fillMaxWidth() // Take up full screen width
            // Outer padding pushes the grey background away from the screen edges
            .padding(horizontal = 24.dp, vertical = 8.dp) // Outer padding
            // Apply the grey background color with rounded corners
            .background(color = SeoulMateBackground, shape = RoundedCornerShape(16.dp)) // Grey background
            // Inner padding pushes the content (icons) away from the edge of the grey background
            .padding(horizontal = 24.dp, vertical = 16.dp), // Inner padding
        verticalArrangement = Arrangement.spacedBy(24.dp) // Space between rows
    ) {
        // We split the list of 12 categories into chunks of 4 to create rows.
        val chunkedCategories = categories.chunked(4)
        chunkedCategories.forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween // Distribute items evenly across the row
            ) {
                rowCategories.forEach { category ->
                    CategoryItem(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        // Modifier.weight(1f) ensures each item takes up equal space.
                        // This corresponds to '0dp' width and '1' weight in XML layouts.
                        modifier = Modifier.weight(1f)
                    )
                }
                // If a row has fewer than 4 items, we add invisible Spacers to fill the remaining slots.
                // This ensures the existing items don't stretch weirdly to fill the row.
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
            .clickable(onClick = onClick), // Make the entire item clickable
        horizontalAlignment = Alignment.CenterHorizontally // Center icon and text horizontally
    ) {
        // Display the icon image
        Image(
            painter = painterResource(id = category.icon),
            contentDescription = category.name, // Accessibility description
            modifier = Modifier
                .size(48.dp) // Fixed size for the icon
                // Shadow removed for cleaner look or adjusted if needed
        )
        Spacer(modifier = Modifier.height(8.dp)) // Space between icon and text
        // Display the category name
        Text(
            text = category.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1, // Ensure text stays on one line
            color = Color(0xFF6C60FD) // Brand color for text
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryGridPreview() {
    CategoryGrid()
}