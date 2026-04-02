package com.project.seoulmate.signup

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