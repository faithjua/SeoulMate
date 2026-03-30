package com.project.seoulmate.ui.screens.addcourse

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun AddCourseScreen(
    navController: NavHostController
) {
    // 지도 카메라 위치 상태 관리 (초기 위치: 서울시청)
    // *사용자 질문에 대한 답변*: 
    // 지도 API에서 '검색'을 구현할 때는, 텍스트 검색 API(예: 네이버 지역 검색 API)를 호출해 
    // 위도(lat), 경도(lng)를 받아온 다음, 아래 cameraPositionState.position 값을 변경하여 지도를 이동시킵니다.
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(37.5666102, 126.9783881), 14.0)
    }

    var searchQuery by remember { mutableStateOf("") }
    var detailLocation by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "코스 추가",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // AI 코스 자동완성 플로팅 버튼
                ExtendedFloatingActionButton(
                    onClick = { /* AI 코스 자동완성 로직 */ },
                    containerColor = Color(0xFF6C60FD),
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp),
                    icon = { Icon(Icons.Default.AutoAwesome, "AI Icon") },
                    text = { Text("AI 코스 자동완성", fontWeight = FontWeight.Bold) }
                )

                // 완료 버튼
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0E0E0), // 초기엔 회색(비활성화 느낌)
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("완료", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 지역 타이틀
            Text(
                text = "지역",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // 네이버 지도 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            ) {
                NaverMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                )

                // 지도 위 검색창 (오버레이)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("지역을 입력해 주세요", color = Color.Gray) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "검색",
                            tint = Color(0xFF6C60FD)
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF6C60FD),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true
                )
            }

            // 추가 설명 영역
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "1",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "지도에서 직접 검색하거나 AI 코스 자동완성하기",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF6C60FD), CircleShape)
                        .clickable { /* 항목 추가 로직 */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "추가",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 세부 장소
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "세부 장소",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(선택 작성)",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                OutlinedTextField(
                    value = detailLocation,
                    onValueChange = { detailLocation = it },
                    placeholder = { Text("예: 태릉입구역 6번 출구", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF6C60FD),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true
                )
            }
        }
    }
}
