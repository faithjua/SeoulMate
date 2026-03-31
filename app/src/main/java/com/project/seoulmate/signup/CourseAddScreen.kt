/*package com.project.seoulmate.signup

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.project.seoulmate.R






// 3. 메인 UI 화면
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseAddScreen(
    viewModel: CourseAddViewModel = viewModel(),
    onCloseClick: () -> Unit = {} // 닫기 버튼 콜백 추가
) {
    val courseList by viewModel.courseLocations.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    // 바텀 시트 상태 관리
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    // 시안에 맞춘 색상 정의
    val backgroundColor = Color(0xFFF4F5F6) // 배경 연한 회색
    val primaryColor = Color(0xFF6C60FD)    // 메인 보라색
    val textColor = Color(0xFF333333)       // 짙은 회색 텍스트
    val labelColor = Color(0xFF666666)      // 연한 회색 라벨

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "코스 추가",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                },
                actions = {
                    IconButton(onClick = onCloseClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = primaryColor // 시안처럼 보라색 아이콘
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = backgroundColor,
        bottomBar = {
            // 🌟 하단 고정 버튼 영역 (시안 맞춤)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 16.dp // 상단 그림자 효과
            ) {
                Button(
                    onClick = { /* 완료 로직 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        disabledContainerColor = Color(0xFFE0E0E0) // 비활성화 시 회색
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = courseList.isNotEmpty() // 장소가 있을 때만 활성화
                ) {
                    Text(
                        text = "완료",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🌟 메인 입력 폼 (흰색 카드)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // 1. 지역 섹션
                    Text(
                        text = "지역",
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0E0E0)), // 지도 배경
                        contentAlignment = Alignment.Center
                    ) {
                        // TODO: 여기에 실제 지도 뷰(NaverMap 등)를 배치하세요!
                        Text("지도 영역", color = textColor.copy(alpha = 0.6f))

                        // 🔍 지도 위에 겹쳐진 검색창 (시안 맞춤)
                        var manualSearchText by remember { mutableStateOf("") }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color.White)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = manualSearchText,
                                onValueChange = { manualSearchText = it },
                                textStyle = TextStyle(
                                    color = textColor,
                                    fontSize = 15.sp
                                ),
                                cursorBrush = SolidColor(textColor),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "검색",
                                            tint = Color(0xFFE0E0E0), // 연한 회색 아이콘
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        if (manualSearchText.isEmpty()) {
                                            Text(
                                                text = "지역을 입력해 주세요",
                                                color = labelColor.copy(alpha = 0.6f),
                                                fontSize = 15.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. 세부 장소 섹션
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "세부 장소",
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(선택 작성)",
                            color = labelColor,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    var detailedPlaceText by remember { mutableStateOf("") }
                    SignupInfoField(
                        label = "", // 라벨은 위에서 따로 배치함
                        value = detailedPlaceText,
                        onValueChange = { detailedPlaceText = it },
                        placeholderText = "예: 태릉입구역 6번 출구"
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // 🌟 AI 코스 자동완성 버튼 (메인 카드 내부 플로팅 버튼)
                    Button(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier
                            .align(Alignment.End)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI 코스 자동완성",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. 장소 리스트 영역
                    if (courseList.isEmpty()) {
                        Text(
                            text = "아직 추가된 장소가 없습니다.",
                            color = labelColor,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn {
                            itemsIndexed(courseList) { index, location ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        modifier = Modifier.width(20.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    // 장소 이름 칩 (시안 맞춤)
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        border = ButtonDefaults.outlinedButtonBorder,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = location.name,
                                            color = textColor,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                        )
                                    }
                                    // 삭제 버튼 (수동 편집 기능)
                                    IconButton(onClick = { viewModel.removeLocation(index) }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "삭제",
                                            tint = labelColor // 연한 회색 아이콘
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. 대망의 AI 프롬프트 바텀 시트 (시안 맞춤)
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            var promptText by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. [SVG 로고 자리] (시안 맞춤 겹치기)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = Color(0xFFF4F5F6), // 사각형 배경색
                            shape = RoundedCornerShape(24.dp) // 모서리 둥글기
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "로고",
                        contentScale = ContentScale.None
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // 2. 타이틀
                Text(
                    text = "무슨 코스를 완성해드릴까요?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(24.dp))

                // 3. 입력창 (시안 맞춤)
                SignupInfoField(
                    label = "",
                    value = promptText,
                    onValueChange = { promptText = it },
                    placeholderText = "예:"
                )
                Spacer(modifier = Modifier.height(24.dp))

                // 4. 코스 완성하기 버튼 (로직 유지)
                Button(
                    onClick = {
                        viewModel.generateCourseFromAi(promptText) {
                            showBottomSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        disabledContainerColor = Color(0xFFE0E0E0) // 비활성화 시 회색
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = promptText.isNotBlank() && !isAiLoading
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI가 코스를 짜는 중...")
                    } else {
                        Text(
                            text = "코스 완성하기",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// 💡 회원가입 화면에서 사용한 SignupInfoField 재활용/커스텀
@Composable
fun SignupInfoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String = "",
    readOnly: Boolean = false
) {
    val primaryColor = Color(0xFF6C60FD)
    val textColor = Color(0xFF333333)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White) // 흰색 배경
                .border(
                    width = 1.dp,
                    color = Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 15.sp
                ),
                cursorBrush = SolidColor(textColor),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholderText,
                            color = Color(0xFFE0E0E0), // 연한 회색 플레이스홀더
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

 */