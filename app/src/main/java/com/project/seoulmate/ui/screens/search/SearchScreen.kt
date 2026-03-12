package com.project.seoulmate.ui.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.project.seoulmate.R
import com.project.seoulmate.ui.navigation.Screen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    navController: NavController
) {
    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Top Bar Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.popBackStack() }
                )

                // Search Bar Box (290 * 42)
                Box(
                    modifier = Modifier
                        .width(290.dp)
                        .height(42.dp)
                        .background(color = Color(0xFFF2F2F2), shape = RoundedCornerShape(21.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "10,000개 이상의 서울 장소 보유!",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }

                // Home Button
                Icon(
                    painter = painterResource(id = R.drawable.ic_home), // Assuming ic_home exists
                    contentDescription = "Home",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Recent Searches
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "최근 검색어",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "모두 지우기",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.clickable { /* TODO: Clear all */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                val recentSearches = listOf("홍대 베이커리", "홍대 베이커리", "홍대", "홍대 베이커리", "홍대 베이커리", "홍대")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    recentSearches.forEach { search ->
                        SearchChip(text = search)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Ad Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF6C60FD)), // Purple color similar to design
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "광고광고광고",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "일석이조의 서울메이트 활동",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
            
            // Adding a small indicator below the ad (as seen in the design)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(Color(0xFF6C60FD))
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(Color(0xFFE0E0E0))
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Trending Searches
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "주간 급상승 검색어",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                val trendingSearches = listOf("홍대 베이커리", "홍대 베이커리", "홍대", "홍대 베이커리", "홍대 베이커리", "홍대")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    trendingSearches.forEach { search ->
                        SearchChip(text = search)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchChip(text: String) {
    // 27dp height chip
    Row(
        modifier = Modifier
            .height(27.dp)
            .border(
                border = BorderStroke(1.dp, Color(0xFFEFEFEF)),
                shape = RoundedCornerShape(13.5.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove",
            tint = Color.Gray,
            modifier = Modifier
                .size(14.dp)
                .clickable { /* TODO: Remove item */ }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    SearchScreen(navController = rememberNavController())
}
