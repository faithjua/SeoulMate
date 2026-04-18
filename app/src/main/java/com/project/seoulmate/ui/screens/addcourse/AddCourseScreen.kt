package com.project.seoulmate.ui.screens.addcourse

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.*
import com.project.seoulmate.R
import com.project.seoulmate.config.AppConfig
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun AddCourseScreen(
    navController: NavHostController,
    viewModel: AddCourseViewModel = hiltViewModel()
) {
    // 이전 화면에서 넘어온 데이터 바구니 꺼내기
    val passedDate = navController.previousBackStackEntry?.savedStateHandle?.get<String>("ai_date") ?: "날짜 미정"
    val passedCategoriesText = navController.previousBackStackEntry?.savedStateHandle?.get<String>("ai_categories") ?: ""
    //  인원과 예산 꺼내기
    val passedMembers = navController.previousBackStackEntry?.savedStateHandle?.get<String>("ai_members") ?: "인원 미정"
    val passedCost = navController.previousBackStackEntry?.savedStateHandle?.get<String>("ai_cost") ?: "예산 미정"
    //  뷰모델 상태 관찰
    val courseList by viewModel.courseLocations.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    // 코스설명 가져오기
    val aiDescription by viewModel.aiDescription.collectAsState()
    // 네이버 검색 상태
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    // AI 생성 에러 메시지 수집
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // 에러 메시지 토스트 표시
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    // 한글 자모 분리 방지를 위한 로컬 TextFieldValue 상태
    var searchTextFieldValue by remember { mutableStateOf(TextFieldValue(searchQuery)) }

    // ViewModel의 query가 외부에서 변경될 때(예: clearQuery) 로컬 상태 동기화
    LaunchedEffect(searchQuery) {
        if (searchTextFieldValue.text != searchQuery) {
            searchTextFieldValue = searchTextFieldValue.copy(text = searchQuery)
        }
    }

    val focusManager = LocalFocusManager.current

    //  바텀 시트 상태
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var promptText by remember { mutableStateOf("") }

    //  로컬 UI 상태
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
                // 버튼 누르면 바로 통신하는게 아니라 바텀 시트를 열어서 프롬프트 입력
                ExtendedFloatingActionButton(
                    onClick = { showBottomSheet = true },
                    containerColor = Color(0xFF6C60FD),
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp),
                    icon = { Icon(Icons.Default.AutoAwesome, "AI Icon") },
                    text = { Text("AI 코스 자동완성", fontWeight = FontWeight.Bold) }
                )

                // 완료 버튼 누르면 서버에 저장 후 ID를 들고 돌아감
                Button(
                    onClick = {
                        // "서울" 등 기본 지역명이 없으므로 첫 번째 장소의 지역 혹은 빈 값으로 처리
                        viewModel.submitFinalCourse(
                            region = "서울",
                            detailLocation = detailLocation,
                            originalPrompt = aiDescription,
                            onSuccess = { savedId ->
                                val finalCourseNames = courseList.map { it.name }
                                navController.previousBackStackEntry?.savedStateHandle?.set("course_id", savedId)
                                navController.previousBackStackEntry?.savedStateHandle?.set("generated_courses", finalCourseNames)
                                navController.previousBackStackEntry?.savedStateHandle?.set("ai_description", aiDescription)
                                navController.popBackStack()
                            },
                            onError = { error ->
                                Timber.tag("CourseSubmit").e("저장 실패: $error")
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (courseList.isNotEmpty()) Color(0xFF6C60FD) else Color(0xFFE0E0E0),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = courseList.isNotEmpty() && !isAiLoading
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("완료", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        // LazyColumn 구조 유지
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
                Column {
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
                        ){
                            //TODO 오류나면 여기 없애자!!
                            //  courseList에 있는 장소들을 돌면서 마커를 찍는 로직 (현재 코드에 없다면 생략 가능)
                            courseList.forEach { location ->
                                // 좌표가 null이 아닐 때만 마커 생성!
                                if (location.lat != null && location.lng != null) {
                                    Marker(
                                        state = MarkerState(position = LatLng(location.lat, location.lng)),
                                        captionText = location.name
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = searchTextFieldValue,
                            onValueChange = {
                                searchTextFieldValue = it
                                viewModel.updateSearchQuery(it.text)
                            },
                            placeholder = { Text("지역을 입력해 주세요", color = Color.Gray) },
                            trailingIcon = {
                                if (searchTextFieldValue.text.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.clearSearchQuery() }) {
                                        Icon(Icons.Default.Close, contentDescription = "지우기", tint = Color.Gray)
                                    }
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = "검색", tint = Color(0xFF6C60FD))
                                }
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
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { focusManager.clearFocus() }
                            )
                        )
                    }

                    // 검색 결과 표시
                    if (searchTextFieldValue.text.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column {
                                if (isSearching) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF6C60FD),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else if (searchResults.isEmpty()) {
                                    Text(
                                        text = "검색 결과가 없습니다",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                } else {
                                    searchResults.forEach { item ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.addPlaceFromSearch(item)
                                                    // 지도 카메라를 해당 위치로 이동
                                                    cameraPositionState.position = CameraPosition(
                                                        LatLng(item.getLatitude(), item.getLongitude()),
                                                        14.0
                                                    )
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                        ) {
                                            Text(
                                                text = item.getCleanTitle(),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.Black
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.getBestAddress(),
                                                fontSize = 13.sp,
                                                color = Color.Gray
                                            )
                                            if (item.category.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = item.category,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF6C60FD)
                                                )
                                            }
                                        }
                                        if (item != searchResults.last()) {
                                            HorizontalDivider(color = Color(0xFFE0E0E0))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            // 코스 리스트 출력
            // 삭제, 화살표로 위아래 순서바꾸기 정도만 구현함. 터치로 자유롭게 바꾸는 것은 상당히 많은 코드 변화 우려
            itemsIndexed(courseList) { index, location ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F8F8), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween // 양끝 정렬
                ) {
                    // 왼쪽: 순번과 장소 이름 (글자가 길어질 것을 대비해 weight(1f) 부여)
                    Column( // Row 내부의 텍스트 영역을 Column으로 감싸줌
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${index + 1}", color = Color(0xFF6C60FD), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(location.name, fontSize = 14.sp)
                        }

                        // TODO 좌표가 없으면 작게 안내 문구를 띄워줌. 좌표 null인 문제는 추후 업데이트로 해결하자
                        if (location.lat == null || location.lng == null) {
                            Text(
                                text = "지도 위치 미지원 장소",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 20.dp, top = 2.dp)
                            )
                        }
                    }

                    // 오른쪽: 위/아래 이동 및 삭제 버튼 묶음
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 맨 위가 아니면 '위로 이동' 버튼 표시
                        if (index > 0) {
                            IconButton(
                                onClick = { viewModel.moveLocation(index, index - 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "위로", tint = Color.Gray)
                            }
                        }

                        // 맨 아래가 아니면 '아래로 이동' 버튼 표시
                        if (index < courseList.lastIndex) {
                            IconButton(
                                onClick = { viewModel.moveLocation(index, index + 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "아래로", tint = Color.Gray)
                            }
                        }

                        // 삭제 버튼
                        IconButton(
                            onClick = { viewModel.removeLocation(index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color(0xFFE53935)) // 삭제는 빨간색 계열로 포인트
                        }
                    }
                }
            }

            // IS_PRODUCTION이 false일 때만 세부 장소 입력란 표시
            item {
                if (!AppConfig.IS_PRODUCTION) {
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
                } else {
                    // IS_PRODUCTION일 때는 하단 패딩만 유지
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // AI 코스 생성 클릭시 뜰 바텀 시트
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