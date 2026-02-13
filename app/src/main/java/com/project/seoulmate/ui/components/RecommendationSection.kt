package com.project.seoulmate.ui.components
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.DrawableRes
import com.project.seoulmate.R
import com.project.seoulmate.ui.theme.SeoulMatePrimary

// @DrawableRes ensures we only pass valid drawable resource IDs (Integers) for the image.
@Composable
fun RecommendationSection(
    modifier: Modifier = Modifier,
    onSeeAllClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // Clip the top corners to be rounded
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(SeoulMatePrimary) // Purple brand background
            .padding(16.dp)
    ) {
        // Header Row: "How about this meeting?" + Arrow Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSeeAllClick), // Make the header clickable
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "이런 만남은 어떤가요?",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "더보기",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal scrolling list for cards.
        // LazyRow is efficient because it only renders items that are currently visible on screen.
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp), // Space between cards
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            // 'items' block is where we define what goes into the list
            items(1) {
                // Card 1
                RecommendationCard(
                    title = "창덕궁 탐방 및 맛집",
                    location = "창덕궁",
                    time = "금 오후 4-5시",
                    price = "₩20,000",
                    rating = "5.0",
                    imageRes = R.drawable.img_recommend_1, // Using placeholder image
                    tags = listOf("혼잡", "#관광", "#한식")
                )
                Spacer(modifier = Modifier.width(16.dp))
                // Card 2
                RecommendationCard(
                    title = "한강 요트 투어",
                    location = "반포한강공원",
                    time = "토 오후 7-9시",
                    price = "₩35,000",
                    rating = "4.8",
                    imageRes = R.drawable.img_recommend_2,
                    tags = listOf("여유", "#관광", "#힐링")
                )
            }
        }
    }
}

@Composable
fun RecommendationCard(
    title: String,
    location: String,
    time: String,
    price: String,
    rating: String,
    @DrawableRes imageRes: Int,
    tags: List<String>
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(280.dp),
        shape = RoundedCornerShape(20.dp), // Rounded corners for the whole card
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Drop shadow
    ) {
        // Box allows us to stack elements on top of each other (layers).
        // Order: Bottom -> Top
        Box(modifier = Modifier.fillMaxSize()) {
            // Layer 1 [Bottom]: Background Image
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null, // Decorative image, no description needed
                contentScale = ContentScale.Crop, // Crop image to fill the bounds
                modifier = Modifier.fillMaxSize()
            )

            // Layer 2 [Middle]: Gradient Overlay
            // This adds a dark shade at the bottom so white text remains readable.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent, // Start clear at top
                                Color.Black.copy(alpha = 0.7f) // Fade to dark at bottom
                            ),
                            startY = 300f // Adjust where the gradient starts
                        )
                    )
            )

            // Layer 3 [Top]: Text and Icon Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween // Push Heart to top, Text to bottom
            ) {
                // Top: Heart Icon
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "찜하기",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Bottom: Info
                Column {
                    // Tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        tags.forEach { tag ->
                            Surface(
                                color = if (tag == "혼잡") Color(0xFFFF6B6B) else Color(0xFF6C60FD),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(22.dp)
                            ) {
                                Text(
                                    text = tag,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Title
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Details
                    Text(
                        text = "$time | 예상 $price",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "평점",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = rating,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun RecommendationSectionPreview() {
    RecommendationSection()
}