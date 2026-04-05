package com.project.seoulmate.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale
import com.project.seoulmate.data.model.MemberRole
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    idToken: String,
    email: String,
    onSignupSuccess: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel()
) {
    var nickname by remember { mutableStateOf("소울이") }

    //  Enum을 상태로 관리합니다! (UI에서는 한글을 보여주고, 데이터는 Enum 객체로 안전하게 보관)
    var selectedRole by remember { mutableStateOf(MemberRole.TRAVELER) }

    var nationality by remember { mutableStateOf("대한민국") }
    var showBottomSheet by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFFF4F5F6)
    val primaryColor = Color(0xFF6C60FD)

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text("회원가입", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Spacer(modifier = Modifier.height(40.dp))

            SignupInfoField("이메일", email, onValueChange = {}, readOnly = true)
            Spacer(modifier = Modifier.height(24.dp))
            SignupInfoField("닉네임", nickname, onValueChange = { nickname = it })
            Spacer(modifier = Modifier.height(24.dp))

            //  역할 선택 (드롭다운)
            RoleDropdownField(
                selectedRole = selectedRole,
                onRoleSelected = { selectedRole = it } // Enum 객체 자체가 넘어옵니다.
            )
            Spacer(modifier = Modifier.height(24.dp))

            //  국적 선택 (클릭 시 바텀 시트 열림)
            NationalitySelectionField(
                selectedNationality = nationality,
                onClick = { showBottomSheet = true }
            )
        }

        // 하단 고정 버튼 영역
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* 뒤로 가기 */ },
                    modifier = Modifier.weight(1f).height(56.dp).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("수정하기", color = Color(0xFF757575), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        //  통신 직전에 Enum 객체를 String(.name)으로 바꿔서 던집니다! (Enum-String-Enum 흐름 완성)
                        viewModel.performSignup(
                            idToken = idToken, email = email, nickname = nickname,
                            role = selectedRole.name, // "TRAVELER" 또는 "GUIDE" 로 변환됨
                            nationality = nationality,
                            onSuccess = { onSignupSuccess() },
                            onError = { message -> Timber.tag("Signup").e(" 실패: $message") }
                        )
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("이대로 완료", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    //  검색 가능한 국적 바텀 시트
    if (showBottomSheet) {
        NationalityBottomSheet(
            onDismiss = { showBottomSheet = false },
            onNationalitySelected = {
                nationality = it
                showBottomSheet = false
            }
        )
    }
}



// 역할 선택 드롭다운 (Enum 사용)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdownField(selectedRole: MemberRole, onRoleSelected: (MemberRole) -> Unit) {
    val primaryColor = Color(0xFF6C60FD)
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("역할 (Role)", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)//타이핑 불가능한 클릭형 드롭다운
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(selectedRole.displayName, color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                //entries()로 대체하는게 좋다는데, 일단 보류
                MemberRole.values().forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role.displayName, color = Color(0xFF333333)) },
                        onClick = { onRoleSelected(role); expanded = false }
                    )
                }
            }
        }
    }
}

//  국적 선택 필드 (누르면 바텀 시트가 뜨는 가짜 TextField)
@Composable
fun NationalitySelectionField(selectedNationality: String, onClick: () -> Unit) {
    val primaryColor = Color(0xFF6C60FD)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("국적", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(10.dp))
                .background(Color.White).clickable { onClick() }.padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(selectedNationality, color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

//  검색 가능한 국가 바텀 시트
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NationalityBottomSheet(onDismiss: () -> Unit, onNationalitySelected: (String) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var searchQuery by remember { mutableStateOf("") }

    // 안전한 Locale.Builder()를 이용한 국가 리스트 추출 (앱 실행 시 한 번만 연산)
    val countries = remember {
        val defaultLocale = Locale.getDefault() // 기기 언어 설정 (한국어면 한국어, 영어면 영어로 표시)
        Locale.getISOCountries().mapNotNull { countryCode ->
            val locale = Locale.Builder().setRegion(countryCode).build() // Deprecated 방지! 최신 방식
            //val name = locale.getDisplayCountry(defaultLocale)
            //if (name.isNotBlank()) name else null
            locale.getDisplayCountry(defaultLocale).ifBlank { null }
        }.distinct().sorted()
    }

    // 검색어에 따른 필터링 결과
    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) countries
        else countries.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text("국적 검색", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

            // 검색창
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("국가를 검색하세요") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 필터링된 국가 리스트
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                items(filteredCountries) { country ->
                    Text(
                        text = country,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNationalitySelected(country) }
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                }
            }
            Spacer(modifier = Modifier.height(24.dp)) // 안드로이드 하단 내비게이션 바 여백
        }
    }
}
/*package com.project.seoulmate.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun SignupScreen(
    idToken: String,
    email: String,
    onSignupSuccess: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel()
) {
    var nickname by remember { mutableStateOf("소울이") }
    var role by remember { mutableStateOf("TRAVELER") }
    var nationality by remember { mutableStateOf("대한민국 (Republic of Korea)") }

    val coroutineScope = rememberCoroutineScope()

    // 시안에 맞춘 색상 정의
    val backgroundColor = Color(0xFFF4F5F6) // 배경 연한 회색
    val primaryColor = Color(0xFF6C60FD)    // 메인 보라색

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)
    ) {
        //  상단 입력 폼 영역
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp) // 하단 버튼 영역만큼 스크롤 확보
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // 1. 타이틀
            Text(
                text = "회원가입",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 2. 입력 필드들
            SignupInfoField(label = "이메일", value = email, onValueChange = {}, readOnly = true)
            Spacer(modifier = Modifier.height(24.dp))

            SignupInfoField(label = "닉네임", value = nickname, onValueChange = { nickname = it })
            Spacer(modifier = Modifier.height(24.dp))

            SignupInfoField(label = "Role (역할: TRAVELER/GUIDE)", value = role, onValueChange = { role = it })
            Spacer(modifier = Modifier.height(24.dp))

            SignupInfoField(label = "국적", value = nationality, onValueChange = { nationality = it })
        }

        //  하단 고정 버튼 영역
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = Color.White,
            shadowElevation = 16.dp // 상단 그림자 효과
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // '수정하기' 버튼 (시안 맞춤)
                Button(
                    onClick = { /* 뒤로 가기 로직 */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0), // 연한 회색 테두리
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "수정하기",
                        color = Color(0xFF757575), // 짙은 회색 텍스트
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // '이대로 완료' 버튼 (로직 유지)
                Button(
                    onClick = {
                        // 2. 직접 통신하지 않고 뷰모델에 위임
                        viewModel.performSignup(
                            idToken = idToken,
                            email = email,
                            nickname = nickname,
                            role = role,
                            nationality = nationality,
                            onSuccess = {
                                android.util.Log.d("Signup", "🔥 가입완료!")
                                onSignupSuccess()
                            },
                            onError = { message ->
                                android.util.Log.e("Signup", "🚨 실패: $message")
                            }
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "이대로 완료",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

 */

//  텍스트 필드를 시안처럼 하얀 배경 + 보라색 글씨로 커스텀
@Composable
fun SignupInfoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false
) {
    val primaryColor = Color(0xFF6C60FD)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            color = primaryColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White) // 시안과 동일한 하얀색 배경
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                textStyle = TextStyle(
                    color = primaryColor, // 시안과 동일한 보라색 입력 텍스트
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                cursorBrush = SolidColor(primaryColor),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

