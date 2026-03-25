package com.project.seoulmate.signup

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

// 1. 코스 장소 데이터 클래스 (나중에 서버 JSON과 매핑될 녀석입니다)
data class CourseLocation(
    val name: String,
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

// 2. 뷰모델: 화면의 상태(장소 리스트, 로딩 상태)를 관리합니다.
class CourseAddViewModel : ViewModel() {
    private val _courseLocations = MutableStateFlow<List<CourseLocation>>(emptyList())
    val courseLocations: StateFlow<List<CourseLocation>> = _courseLocations.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // AI에게 프롬프트를 보내고 결과를 받아오는 가짜(Dummy) 함수
    fun generateCourseFromAi(prompt: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true

            // 💡 실제로는 여기서 Spring Boot API를 호출합니다! (현재는 2초 대기하는 척)
            delay(2000)

            // AI가 뱉어낸 JSON 결과라고 가정하고, 기존 리스트를 싹 '덮어쓰기' 합니다.
            val dummyResult = listOf(
                CourseLocation("경복궁"),
                CourseLocation("북촌 한옥마을"),
                CourseLocation("안국역 한옥 카페"),
                CourseLocation("광장시장 자매집")
            )
            _courseLocations.value = dummyResult

            _isAiLoading.value = false
            onComplete() // 바텀 시트를 닫으라고 UI에 신호를 줍니다.
        }
    }

    // 수동으로 장소를 1개씩 맨 뒤에 추가하는 기능
    fun addLocationManual(name: String) {
        if (name.isNotBlank()) {
            val currentList = _courseLocations.value.toMutableList()
            currentList.add(CourseLocation(name)) // 유저가 친 장소를 쏙 넣음
            _courseLocations.value = currentList
        }
    }

    // 수동으로 장소 삭제하는 기능 (하이브리드 UX의 핵심!)
    fun removeLocation(index: Int) {
        val currentList = _courseLocations.value.toMutableList()
        currentList.removeAt(index)
        _courseLocations.value = currentList
    }
}

// 3. 메인 UI 화면
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseAddScreen(viewModel: CourseAddViewModel = viewModel()) {
    val courseList by viewModel.courseLocations.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    // 바텀 시트 상태 관리
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var promptText by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            // 🌟 AI 코스 생성 버튼 (우측 하단 플로팅 버튼)
            ExtendedFloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = Color(0xFF7B61FF), // 앱 테마색 (보라색)
                contentColor = Color.White
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI")
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI 코스 자동 완성")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 상단 타이틀
            Text("코스 추가", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // 가짜 지도 영역 (나중에 네이버 지도 뷰가 들어갈 자리)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("지도 영역 (마커들이 찍힐 곳)", color = Color.DarkGray)
            }
            Spacer(modifier = Modifier.height(24.dp))
// --- [여기에 수동 검색창 추가!] ---
            var manualSearchText by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualSearchText,
                    onValueChange = { manualSearchText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("지도에서 검색하거나 직접 입력하세요") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.addLocationManual(manualSearchText)
                        manualSearchText = "" // 텍스트 비우기
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("추가")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // ---------------------------------

            // 이 밑으로는 원래 있던 장소 리스트 영역(if (courseList.isEmpty()) ...)이 이어집니다.

            // 장소 리스트 영역 (AI가 채워줄 곳)
            if (courseList.isEmpty()) {
                Text("아직 추가된 장소가 없습니다.\n우측 하단 버튼을 눌러 AI에게 코스를 부탁해보세요!", color = Color.Gray)
            } else {
                LazyColumn {
                    itemsIndexed(courseList) { index, location ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text("${index + 1}", fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.width(16.dp))
                            // 장소 이름 칩
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                border = ButtonDefaults.outlinedButtonBorder,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = location.name,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                            // 삭제 버튼 (수동 편집 기능)
                            IconButton(onClick = { viewModel.removeLocation(index) }) {
                                Icon(Icons.Default.Close, contentDescription = "삭제", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. 대망의 AI 프롬프트 바텀 시트
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🤖 AI에게 어떤 투어를 원하시나요?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("예: 외국인 친구랑 갈 종로 3시간 맛집 투어") },
                    enabled = !isAiLoading
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.generateCourseFromAi(promptText) {
                            // 통신이 끝나면 바텀 시트를 닫고 텍스트를 비웁니다.
                            showBottomSheet = false
                            promptText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                    enabled = promptText.isNotBlank() && !isAiLoading
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI가 코스를 짜는 중...")
                    } else {
                        Text("코스 생성하기")
                    }
                }
            }
        }
    }
}