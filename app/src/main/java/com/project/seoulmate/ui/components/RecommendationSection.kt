package com.project.seoulmate.ui.components

import com.project.seoulmate.R
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import com.project.seoulmate.data.model.Meeting

/**
 * 최근 본 만남 가로 스크롤 섹션.
 *
 * @param meetings ViewModel에서 내려보낸 만남 목록
 * @param onSeeAllClick "더보기" 클릭 콜백
 */
@Composable
fun RecommendationSection(
    title: String = "최근 본 만남",
    modifier: Modifier = Modifier,
    meetings: List<Meeting> = emptyList(),
    onSeeAllClick: () -> Unit = {},
    onMeetingClick: (String) -> Unit = {},
    onFavoriteClick: (String, Boolean) -> Unit = { _, _ -> }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // 헤더 Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable(onClick = onSeeAllClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "더보기",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 가로 스크롤 카드 목록
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            items(meetings) { meeting ->
                RecommendationCard(
                    meeting = meeting,
                    onClick = onMeetingClick,
                    onFavoriteClick = onFavoriteClick
                )
            }
        }
    }
}

/**
 * 만남 카드 하나. Meeting 데이터 클래스를 받아 UI를 그립니다.
 */
@Composable
fun RecommendationCard(
    meeting: Meeting,
    onClick: (String) -> Unit = {},
    onFavoriteClick: (String, Boolean) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(340.dp)
            .clickable { onClick(meeting.id) },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 상단: 이미지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (meeting.imageUrl != null) {
                    AsyncImage(
                        model = meeting.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = painterResource(id = if (meeting.imageRes != 0) meeting.imageRes else R.drawable.img_recommend_1),
                        placeholder = painterResource(id = if (meeting.imageRes != 0) meeting.imageRes else R.drawable.img_recommend_1)
                    )
                } else {
                    Image(
                        painter = painterResource(id = if (meeting.imageRes != 0) meeting.imageRes else R.drawable.img_recommend_1),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 하단 태그 가독성을 위한 그라데이션 오버레이
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )

                // 태그 (하단 좌측)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    meeting.tags.forEach { tag ->
                        Surface(
                            color = when (tag) {
                                "혼잡" -> Color(0xFFFF6B6B)
                                "여유" -> Color(0xFF6CF0A0)
                                "정보 없음" -> Color(0xFF9E9E9E) // 회색
                                else -> Color(0xFF8B80FF)
                            },
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = tag,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // 좋아요 아이콘 (우측 상단)
                Icon(
                    imageVector = if (meeting.isFavorited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (meeting.isFavorited) "찜 취소" else "찜하기",
                    tint = if (meeting.isFavorited) Color(0xFFFF6B6B) else Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(28.dp)
                        .align(Alignment.TopEnd)
                        .clickable { onFavoriteClick(meeting.id, meeting.isFavorited) }
                )
            }

            // 하단: 상세 정보 (보라색 배경)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF7A6BFF), Color(0xFF6C60FD))
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = meeting.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${meeting.time} 예상 ${meeting.price}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "평점",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = meeting.rating,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                        Button(
                            onClick = { /* TODO */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                // 원래는 메이트 신청 버튼
                                text = "만남 보기",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
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