package com.project.seoulmate.signup

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.project.seoulmate.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginTextClick: () -> Unit = {} //  하단 '로그인' 텍스트를 눌렀을 때의 동작을 위함
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val WEB_CLIENT_ID = "103184785151-cmr2evs6iu8kau7fi7oqgu5ajig7a1mo.apps.googleusercontent.com"

    // 시안에 맞는 배경 단색 컬러 (프로젝트 테마에 맞춰 미세조정 가능)
    val backgroundColor = Color(0xFF6C60FD)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)
    ) {
        //  상단 컨텐츠 영역 (아이콘, 타이틀, 서브타이틀)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(bottom = 120.dp), // 하단 버튼 공간 확보를 위해 위로 살짝 올림
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. [SVG 아이콘 자리]
            Box(
                modifier = Modifier
                    .size(120.dp)
                    // ① 둥근 사각형 모양의 은은한 그림자 (선택사항)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp))
                    // ② 사각형 배경색과 둥근 모서리 설정
                    .background(
                        color = Color(0xFF6C60FD), // 시안의 사각형 배경색(Hex 코드)
                        shape = RoundedCornerShape(32.dp) // 모서리 둥글기
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

            // 2. SEOUL MATE 타이틀 (MATE만 굵게 처리)
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                        append("SEOUL ")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                        append("MATE")
                    }
                },
                fontSize = 32.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. 서브타이틀
            Text(
                text = "K-뷰티·덕질·탐방·클래스\n모두 동네의 서울 메이트와 함께",
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }

        //  하단 탭 영역 (버튼, 로그인 텍스트)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 4. 시작하기 버튼 (여기에 기존 구글 로그인 로직을 연결해두었습니다)
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setServerClientId(WEB_CLIENT_ID)
                                .setFilterByAuthorizedAccounts(false)
                                .setAutoSelectEnabled(true)
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val result = credentialManager.getCredential(context, request)
                            val credential = result.credential

                            if (credential is GoogleIdTokenCredential) {
                                val googleIdToken = credential.idToken
                                val email = credential.id ?: ""
                                val nickname = credential.displayName ?: "무명 여행자"

                                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val user = FirebaseAuth.getInstance().currentUser
                                            user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                                                if (tokenTask.isSuccessful) {
                                                    val firebaseToken = tokenTask.result?.token
                                                    if (firebaseToken != null) {
                                                        viewModel.loginToServer(firebaseToken, email, nickname)
                                                    }
                                                }
                                            }
                                        } else {
                                            Log.e("GoogleLogin", "Firebase 인증 실패", task.exception)
                                        }
                                    }
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
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp) // 시안에 맞춘 둥근 사각형
            ) {
                Text(
                    text = "시작하기",
                    color = Color(0xFF333333), // 시안처럼 짙은 회색 텍스트
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. 하단 로그인 안내 텍스트
            Text(
                text = buildAnnotatedString {
                    append("이미 계정이 있나요? ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("로그인")
                    }
                },
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { onLoginTextClick() } // 클릭 시 로그인 화면으로 넘어가게 세팅
                    .padding(8.dp) // 클릭 영역을 조금 넓혀주는 센스
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}