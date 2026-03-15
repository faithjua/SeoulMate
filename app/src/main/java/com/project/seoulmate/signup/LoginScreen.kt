package com.project.seoulmate.signup


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(), //  뷰모델 연결!
    onGoogleLoginClick: () -> Unit = {} //  밖에서 구글 로그인 기능을 연결할 수 있게 비워둡니다
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    //  Firebase 콘솔에서 복사해 둔 Web 클라이언트 ID를 여기에 넣으세요!
    val WEB_CLIENT_ID = "103184785151-cmr2evs6iu8kau7fi7oqgu5ajig7a1mo.apps.googleusercontent.com"

    // 배경 그라데이션 (회원가입 화면과 동일한 테마)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF7B61FF),
            Color(0xFF8C64FF),
            Color(0xFFFFD1E8)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // 1. 앱 로고 및 타이틀 영역
            Text(
                text = "SeoulMate", // 서비스명으로 변경하세요!
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "현지인과 함께하는 진짜 여행",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(80.dp))

            // 2. 구글 로그인 버튼
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)

                            // 구글 로그인 팝업창 세팅
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setServerClientId(WEB_CLIENT_ID)
                                .setFilterByAuthorizedAccounts(false)
                                .setAutoSelectEnabled(true)
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            //  여기서 스마트폰 바닥에서 계정 선택창이 스윽 올라옵니다!
                            val result = credentialManager.getCredential(context, request)
                            val credential = result.credential

                            // 구글에서 무사히 정보를 받아왔다면?
                            if (credential is GoogleIdTokenCredential) {
                                val idToken = credential.idToken // 🔑 가장 중요한 핵심 키!
                                val email = credential.id ?: ""
                                val nickname = credential.displayName ?: "무명 여행자"

                                Log.d("GoogleLogin", "토큰 발급 성공! 뷰모델로 넘깁니다.")

                                //  UI의 임무는 끝. 알아서 서버로 다녀오라고 뷰모델에게 토스합니다!
                                viewModel.loginToServer(idToken, email, nickname)
                            }
                        } catch (e: GetCredentialException) {
                            Log.e("GoogleLogin", "로그인 창 닫힘 또는 에러: ${e.message}")
                        } catch (e: Exception) {
                            Log.e("GoogleLogin", "기타 에러: ${e.message}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp)), // 둥글고 그림자 있는 버튼
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White // 구글 기본 스타일인 흰색 배경
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                // 구글 'G' 로고가 있다면 Row 안에 Image를 넣으면 더 완벽합니다.
                Text(
                    text = "Google로 계속하기",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}