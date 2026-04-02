package com.project.seoulmate.signup

import android.util.Log
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.project.seoulmate.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginTextClick: () -> Unit = {} //  하단 '로그인' 텍스트를 눌렀을 때의 동작을 위함
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 자동 로그인 중인지 파악하는 상태값 (이때는 버튼 대신 로딩바를 보여주기 위함)
    var isAutoLoginChecking by remember { mutableStateOf(true) }

    val WEB_CLIENT_ID = "103184785151-cmr2evs6iu8kau7fi7oqgu5ajig7a1mo.apps.googleusercontent.com"

    //  화면이 처음 켜질 때 딱 한 번 실행되는 자동 로그인 로직
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // 1. 이미 스마트폰에 구글 로그인 기록이 남아있는 경우! (자동 로그인 진행)
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseToken = task.result?.token
                    val email = currentUser.email ?: "" // Firebase에서 이메일 꺼냄

                    if (firebaseToken != null) {
                        // 유저가 버튼을 누르지 않았지만, 뷰모델을 통해 조용히 서버로 로그인 요청을 쏩니다!
                        viewModel.loginToServer(firebaseToken, email)
                    }
                } else {
                    // 토큰이 만료되었거나 에러가 나면 유저가 직접 버튼을 누르게 유도
                    isAutoLoginChecking = false
                }
            }
        } else {
            // 2. 앱을 처음 깔았거나 로그아웃 한 경우 -> 버튼을 보여줌
            isAutoLoginChecking = false
        }
    }
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

        // 하단 탭 영역 (버튼)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 버튼과 로딩바가 번갈아 나타날 고정 크기의 Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), //  버튼 높이로 고정하여 UI 덜컹거림 방지
                contentAlignment = Alignment.Center
            ) {
                if (isAutoLoginChecking) {
                    // 배경이 보라색이므로 하얀색 로딩바가 잘 보입니다.
                    androidx.compose.material3.CircularProgressIndicator(color = Color.White)
                } else {
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

                                        val firebaseCredential =
                                            GoogleAuthProvider.getCredential(googleIdToken, null)
                                        FirebaseAuth.getInstance()
                                            .signInWithCredential(firebaseCredential)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val user =
                                                        FirebaseAuth.getInstance().currentUser
                                                    user?.getIdToken(true)
                                                        ?.addOnCompleteListener { tokenTask ->
                                                            if (tokenTask.isSuccessful) {
                                                                val firebaseToken =
                                                                    tokenTask.result?.token
                                                                val email =
                                                                    credential.id ?: "" // 구글 계정 이메일
                                                                if (firebaseToken != null) {
                                                                    viewModel.loginToServer(
                                                                        firebaseToken,
                                                                        email
                                                                    )
                                                                }
                                                            }
                                                        }
                                                } else {
                                                    Log.e(
                                                        "GoogleLogin",
                                                        "Firebase 인증 실패",
                                                        task.exception
                                                    )
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
                        modifier = Modifier.fillMaxSize(), // 부모 Box(56.dp)를 가득 채움
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White), // 시안에 맞게 하얀 버튼
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "시작하기",
                            color = Color(0xFF333333), // 시안처럼 짙은 회색 텍스트
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

        }
    }
}

