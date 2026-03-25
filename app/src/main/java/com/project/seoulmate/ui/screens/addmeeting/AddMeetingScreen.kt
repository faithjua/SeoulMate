package com.project.seoulmate.ui.screens.addmeeting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.project.seoulmate.ui.components.*
import com.project.seoulmate.ui.navigation.Screen

/**
 * 만남 등록 화면 Composable.
 * @param navController 화면 이동을 위한 NavController
 * @param viewModel Hilt가 자동으로 주입하는 AddMeetingViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeetingScreen(
    navController: NavHostController,
    viewModel: AddMeetingViewModel = hiltViewModel()
) {
    // 폼 전체 상태를 하나의 StateFlow로 수집
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    // 저장/등록 완료 이벤트 수집
    val uiEvent by viewModel.uiEvent.collectAsStateWithLifecycle()

    // uiEvent 처리: NavigateBack 이벤트 발생 시 이전 화면(홈)으로 이동
    LaunchedEffect(uiEvent) {
        if (uiEvent is AddMeetingUiEvent.NavigateBack) {
            navController.popBackStack()
            viewModel.onEventConsumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "만남정보",
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() }, // Cancel
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Text("취소", fontSize = 16.sp, color = Color.Gray)
                }
                Button(
                    onClick = { viewModel.registerMeeting() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C60FD)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text("등록하기", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 사진 업로드 섹션
            PhotoUploadSection(photoCount = formState.photoCount)

            // 만남명
            FormSection(title = "만남명", required = true) {
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { viewModel.updateMeetingName(it) },
                    placeholder = { Text("(최대 10자)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // 카테고리/태그 섹션
            FormSection(title = "카테고리/태그", required = true) {
                CategoryTagSection(
                    selectedCategories = formState.selectedCategories,
                    onCategoryToggle = { viewModel.toggleCategory(it) }
                )
            }

            // 코스 섹션
            FormSection(title = "코스", required = true) {
                CourseSection(
                    courses = formState.courses,
                    onAddClick = { navController.navigate(Screen.CourseAdd.route) }, // TODO: 실제 라우트로 변경
                    onRemoveCourse = { viewModel.removeCourse(it) }
                )
            }

            // 요일/시간 섹션
            FormSection(title = "요일/시간", required = true) {
                TimeSlotSection(
                    timeSlots = formState.timeSlots,
                    onAddClick = { navController.navigate("add_time_slot") } // TODO: 실제 라우트로 변경
                )
            }

            // 만남 소개
            FormSection(title = "만남 소개", required = false) {
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    placeholder = { Text("무엇을 할 것인가요?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // 예상 지출
            FormSection(title = "예상 지출", required = false) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = formState.expectedCost,
                        onValueChange = { viewModel.updateExpectedCost(it) },
                        placeholder = { Text("예상 금액") },
                        modifier = Modifier.weight(1f),
                        trailingIcon = { Text("₩") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Button(
                        onClick = { viewModel.clearExpectedCost() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C60FD)
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(56.dp)
                    ) {
                        Text("없음", color = Color.White)
                    }
                }
            }

            // 모집 인원
            FormSection(title = "모집 인원", required = false) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = formState.minMembers,
                            onValueChange = { viewModel.updateMinMembers(it) },
                            placeholder = { Text("최소 인원") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = formState.maxMembers,
                            onValueChange = { viewModel.updateMaxMembers(it) },
                            placeholder = { Text("최대 인원") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                    Text(
                        text = "최대 인원 모집 시 해당 포스팅이 자동으로 숨겨집니다.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * 폼 섹션 공통 레이아웃 컴포넌트 (변경 없음).
 * 제목 + 필수 * 표시 + 컨텐츠 슬롯
 */
@Composable
fun FormSection(
    title: String,
    required: Boolean,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (required) {
                Text(
                    text = " *",
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        content()
    }
}
