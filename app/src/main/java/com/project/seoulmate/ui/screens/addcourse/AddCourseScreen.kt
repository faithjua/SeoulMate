package com.project.seoulmate.ui.screens.addcourse

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.*
import com.project.seoulmate.R
import com.project.seoulmate.signup.CourseAddViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun AddCourseScreen(
    navController: NavHostController,
    viewModel: CourseAddViewModel = hiltViewModel()
) {
    // 💡 1. 이전 화면에서 넘어온 데이터 바구니 꺼내기
    val passedDate = navController.previousBackStackEntry?.savedStateHandle?.get<String>("ai_date") ?: "날짜 미정"
    val passedCategoriesText = navController.previousBackStackEntry?.savedStateHandle?.get<String>("ai_categories") ?: ""
    // 💡 [추가된 부분] 인원과 예산 꺼내기
    val passedMembers = navController.previousBackStackEntry?.savedStateHandle?.get<String>("ai_members") ?: "인원 미정"
    val passedCost = navController.previousBackStackEntry?.savedStateHandle?.get<String>("ai_cost") ?: "예산 미정"
    // 2. 뷰모델 상태 관찰
    val courseList by viewModel.courseLocations.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    // 코스설명 가져오기
    val aiDescription by viewModel.aiDescription.collectAsState()

    // 💡 3. 바텀 시트 상태 부활!
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var promptText by remember { mutableStateOf("") }

    // 4. 로컬 UI 상태
    var searchQuery by remember { mutableStateOf("") }
    var detailLocation by remember { mutableStateOf("") }

    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(37.5666102, 126.9783881), 14.0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("코스 추가", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                // 💡버튼 누르면 바로 통신하는게 아니라 바텀 시트를 엽니다!
                ExtendedFloatingActionButton(
                    onClick = { showBottomSheet = true },
                    containerColor = Color(0xFF6C60FD),
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp),
                    icon = { Icon(Icons.Default.AutoAwesome, "AI Icon") },
                    text = { Text("AI 코스 자동완성", fontWeight = FontWeight.Bold) }
                )

                // 💡 [수정] 완료 버튼 누르면 리스트를 바구니에 담아 돌아갑니다.
                Button(
                    onClick = {
                        val finalCourseNames = courseList.map { it.name }
                        navController.previousBackStackEntry?.savedStateHandle?.set("generated_courses", finalCourseNames)
                        // AI가 써준 코스 설명도 같이 보냄
                        navController.previousBackStackEntry?.savedStateHandle?.set("ai_description", aiDescription)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (courseList.isNotEmpty()) Color(0xFF6C60FD) else Color(0xFFE0E0E0),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = courseList.isNotEmpty()
                ) {
                    Text("완료", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        // LazyColumn 구조 유지 (동료분 코드)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // 지역 타이틀
                Text(
                    text = "지역",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
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

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("지역을 입력해 주세요", color = Color.Gray) },
                        trailingIcon = { Icon(Icons.Default.Search, contentDescription = "검색", tint = Color(0xFF6C60FD)) },
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
            }

            item {
                // 추가 설명 영역
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFF6C60FD), CircleShape)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) "지도에서 검색해 주세요" else searchQuery,
                            color = if (searchQuery.isEmpty()) Color.Gray else Color.Black,
                            fontSize = 14.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF6C60FD), CircleShape)
                            .clickable {
                                if (searchQuery.isNotEmpty()) {
                                    viewModel.addLocationManual(searchQuery)
                                    searchQuery = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "추가", tint = Color.White)
                    }
                }
            }

            // 🌟 코스 리스트 출력 (동료분 디자인 유지)
            itemsIndexed(courseList) { index, location ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F8F8), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1}", color = Color(0xFF6C60FD), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(location.name, fontSize = 14.sp)
                    }
                    IconButton(onClick = { viewModel.removeLocation(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color.Gray)
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("세부 장소", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("(선택 작성)", fontSize = 14.sp, color = Color.Gray)
                    }

                    OutlinedTextField(
                        value = detailLocation,
                        onValueChange = { detailLocation = it },
                        placeholder = { Text("예: 태릉입구역 6번 출구", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp),
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6C60FD),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        singleLine = true
                    )
                }
            }
        }
    }

    // 🌟 AI 코스 생성 클릭시 뜰 바텀 시트
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            val primaryColor = Color(0xFF6C60FD)
            val textColor = Color(0xFF333333)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(color = Color(0xFFF4F5F6), shape = RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "로고",
                        contentScale = ContentScale.None
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text("무슨 코스를 완성해드릴까요?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(16.dp))

                Text("일정: $passedDate | 테마: $passedCategoriesText\n인원: $passedMembers | 예산: $passedCost", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        textStyle = TextStyle(color = Color(0xFF333333), fontSize = 15.sp),
                        cursorBrush = SolidColor(Color(0xFF6C60FD)),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (promptText.isEmpty()) {
                                Text("예: 외국인 친구랑 갈 종로 3시간 맛집 투어", color = Color(0xFFBDBDBD), fontSize = 15.sp)
                            }
                            innerTextField()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val categoryList = passedCategoriesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        viewModel.generateCourseFromAi(
                            date = passedDate,
                            categories = categoryList,
                            members = passedMembers,
                            budget = passedCost,
                            prompt = promptText
                        ) {
                            showBottomSheet = false
                            promptText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        disabledContainerColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = promptText.isNotBlank() && !isAiLoading
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI가 코스를 짜는 중...")
                    } else {
                        Text("코스 완성하기", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}