package com.project.seoulmate.ui.screens.addmeeting

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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

    /**
     * 카테고리, 장소/시간 정보 공유를 위한 추가 코드
     */
    //  0. 코스 화면에서 AI가 짠 코스를 들고 돌아왔을 때 받아주는 로직
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val returnedCourses by savedStateHandle?.getStateFlow<List<String>>("generated_courses", emptyList())
        ?.collectAsStateWithLifecycle(initialValue = emptyList()) ?: remember{mutableStateOf(emptyList())}

    //  1. 돌아온 AI 설명 받는 로직 추가
    val returnedDescription by savedStateHandle?.getStateFlow<String>("ai_description", "")
        ?.collectAsStateWithLifecycle(initialValue = "") ?: remember{mutableStateOf("")}

    //  2. 서버에서 저장된 실제 코스 ID 받는 로직 추가
    val returnedCourseId by savedStateHandle?.getStateFlow<Long?>("course_id", null)
        ?.collectAsStateWithLifecycle(initialValue = null) ?: remember{mutableStateOf(null)}

    //  3. LaunchedEffect에서 코스, 설명, ID를 모두 처리하도록 수정
    LaunchedEffect(returnedCourses, returnedDescription, returnedCourseId) {
        if (returnedCourses.isNotEmpty()) {
            // UI 표시용 코스 이름들
            returnedCourses.forEach { viewModel.addCourse(it) }
            savedStateHandle?.remove<List<String>>("generated_courses")
        }

        if (returnedDescription.isNotBlank()) {
            // AI가 써준 설명을 [만남 소개] 폼 상태에 덮어쓰기!
            viewModel.updateDescription(returnedDescription)
            savedStateHandle?.remove<String>("ai_description")
        }

        if (returnedCourseId != null) {
            // 서버에 실제 저장된 코스 ID를 만남 폼에 저장
            viewModel.updateCourseId(returnedCourseId!!)
            savedStateHandle?.remove<Long>("course_id")
        }
    }
    //여기까지 추가


    // uiEvent 처리: NavigateBack 이벤트 발생 시 이전 화면(홈)으로 이동
    LaunchedEffect(uiEvent) {
        if (uiEvent is AddMeetingUiEvent.NavigateBack) {
            navController.popBackStack()
            viewModel.onEventConsumed()
        }
    }

    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    // 이미지 선택기
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.uploadImages(uris) { success, errorMessage ->
                if (success) {
                    Toast.makeText(context, "이미지 업로드 완료", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, errorMessage ?: "업로드 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = Color(0xFFDBDBDB),
        unfocusedBorderColor = Color(0xFFDBDBDB),
        focusedTextColor = Color.Gray,
        unfocusedTextColor = Color(0xFFDBDBDB),
        focusedPlaceholderColor = Color(0xFFDBDBDB),
        unfocusedPlaceholderColor = Color(0xFFDBDBDB)
    )

    Scaffold(
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
        ) {
            // 탑 구역 (선 없는 단색 배경색으로 구분 효과)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Color(0xFFF7F7F7))
                    .padding(bottom = 24.dp)
            ) {
                TopAppBar(
                    title = { Text(text = "만남정보", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "닫기")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    PhotoUploadSection(
                        imageUrls = formState.imageUrls,
                        onGalleryClick = { imagePickerLauncher.launch("image/*") }
                    )
                }
            }

            // 하단 폼 구역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 만남명
            FormSection(title = "만남명", required = true) {
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { viewModel.updateMeetingName(it) },
                    placeholder = { Text("(최대 10자)") },
                    modifier = Modifier.width(368.dp).height(59.dp),
                    singleLine = true,
                    colors = customTextFieldColors,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // 카테고리/태그 섹션
            FormSection(title = "카테고리/태그", required = true) {
                CategoryTagSection(
                    selectedCategories = formState.selectedCategories,
                    onCategoryToggle = { viewModel.toggleCategory(it) }
                )
            }

            //  [수정] 2. 코스 섹션: 버튼 누를 때 날짜와 카테고리를 바구니에 담아 출발!
            FormSection(title = "코스", required = true) {
                CourseSection(
                    courses = formState.courses,
                    onAddClick = {
                        // timeSlots에서 첫 번째 값을 날짜로, 선택된 카테고리들을 쉼표로 연결
                        val dateToPass = formState.timeSlots.firstOrNull() ?: "날짜 미정"
                        val categoriesToPass = formState.selectedCategories.joinToString(", ")
                        //  [추가된 부분] 인원과 예산 데이터 다듬기 (비어있을 경우 예외 처리)
                        val minMem = formState.minMembers.ifBlank { "제한 없음" }
                        val maxMem = formState.maxMembers.ifBlank { "제한 없음" }
                        val cost = formState.expectedCost.ifBlank { "제한 없음" }

                        //  [추가된 부분] 바구니에 통째로 담기
                        navController.currentBackStackEntry?.savedStateHandle?.apply {
                            set("ai_date", dateToPass)
                            set("ai_categories", categoriesToPass)
                            set("ai_members", "${minMem}명 ~ ${maxMem}명")
                            set("ai_cost", cost)
                        }

                        navController.navigate("add_course")
                    },
                    onRemoveCourse = { viewModel.removeCourse(it) }
                )
            }

            // 요일/시간 섹션
            FormSection(title = "요일/시간", required = true) {
                TimeSlotSection(
                    timeSlots = formState.timeSlots,
                    onAddClick = { showDatePicker = true },
                    onRemoveClick = { index -> viewModel.removeTimeSlot(index) }
                )
            }

            // 만남 소개
            FormSection(title = "만남 소개", required = false) {
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    placeholder = { Text("무엇을 할 것인가요?") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    colors = customTextFieldColors,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // 예상 지출
            FormSection(title = "예상 지출", required = true) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = formState.expectedCost,
                        onValueChange = { viewModel.updateExpectedCost(it) },
                        placeholder = { Text("예상 금액") },
                        modifier = Modifier.weight(1f).height(59.dp),
                        trailingIcon = { Text("₩", color = Color(0xFFDBDBDB)) },
                        singleLine = true,
                        colors = customTextFieldColors,
                        shape = RoundedCornerShape(8.dp)
                    )
                    /*
                    Button(
                        onClick = { viewModel.clearExpectedCost() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C60FD)
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        modifier = Modifier.height(59.dp)
                    ) {
                        Text("없음", color = Color.White)
                    }
                    */
                }
            }

            // 모집 인원
            FormSection(title = "모집 인원", required = true) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = formState.minMembers,
                            onValueChange = { viewModel.updateMinMembers(it) },
                            placeholder = { Text("최소 인원") },
                            modifier = Modifier.weight(1f).height(59.dp),
                            singleLine = true,
                            colors = customTextFieldColors,
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = formState.maxMembers,
                            onValueChange = { viewModel.updateMaxMembers(it) },
                            placeholder = { Text("최대 인원") },
                            modifier = Modifier.weight(1f).height(59.dp),
                            singleLine = true,
                            colors = customTextFieldColors,
                            shape = RoundedCornerShape(8.dp)
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

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                    if (selectedDateMillis != null) {
                        showTimePicker = true
                    }
                }) {
                    Text("날짜 선정 완료")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // Combine date and time
                    if (selectedDateMillis != null) {
                        val date = Date(selectedDateMillis!!)
                        // Create formatter for the date part: "8월 18일 (월)"
                        val dateFormatter = SimpleDateFormat("M월 d일 (E)", Locale.KOREA)
                        dateFormatter.timeZone = TimeZone.getTimeZone("UTC") // Material DatePicker returns UTC millis
                        val dateString = dateFormatter.format(date)

                        // Parse time piece (e.g. 오후 7시)
                        val amPm = if (timePickerState.hour < 12) "오전" else "오후"
                        val hour12 = if (timePickerState.hour % 12 == 0) 12 else timePickerState.hour % 12
                        val minute = timePickerState.minute
                        val timeString = if (minute == 0) {
                            "$amPm ${hour12}시"
                        } else {
                            // "오후 7시 30분" 형식
                            "$amPm ${hour12}시 ${minute}분"
                        }

                        val finalFormattedString = "$dateString $timeString"
                        viewModel.addTimeSlot(finalFormattedString)

                        // ISO 형식 날짜/시간 생성 (서버 전송용)
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = selectedDateMillis!!
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                        isoFormatter.timeZone = TimeZone.getTimeZone("UTC")
                        val isoDateString = isoFormatter.format(calendar.time)
                        viewModel.updateMeetDate(isoDateString)
                    }
                    showTimePicker = false
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("취소")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
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
