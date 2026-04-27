package com.project.seoulmate.signup

import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.project.seoulmate.R
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToSignup: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 1. ViewModel의 상태를 관찰합니다! (무한 로딩 방지의 핵심)
    val loginState by viewModel.loginState.collectAsState()

    // Firebase 토큰을 가져오는 극초반 로딩 상태
    var isFirebaseLoading by remember { mutableStateOf(true) }

    val WEB_CLIENT_ID = "103184785151-cmr2evs6iu8kau7fi7oqgu5ajig7a1mo.apps.googleusercontent.com"

    // 2. ViewModel 상태 변화에 따른 UI 처리 (네비게이션 및 에러 메시지 팝업)
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                Timber.tag("LoginScreen").d("로그인 성공! 다음 화면으로 이동")
                val member = (loginState as LoginState.Success).member
                if (member.isNewMember) {
                    onNavigateToSignup()
                } else {
                    onNavigateToHome()
                }
            }
            is LoginState.Error -> {
                val errorMessage = (loginState as LoginState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful && task.result?.token != null) {
                    val firebaseToken = task.result!!.token!!
                    val email = currentUser.email ?: ""

                    viewModel.loginToServer(firebaseToken, email)
                    isFirebaseLoading = false
                } else {
                    isFirebaseLoading = false
                    Toast.makeText(context, "구글 로그인 상태를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            isFirebaseLoading = false
        }
    }

    // 3. 실제 로딩바를 보여줄 조건 = (Firebase 조회 중) OR (서버 API 통신 중)
    val showLoadingBar = isFirebaseLoading || loginState is LoginState.Loading

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // ✨ 변경점 1: 단색 배경 → 이미지 배경으로 교체
        // 화면 전체를 덮는 배경 이미지 (피그마에서 export한 노이즈 텍스처 포함 배경)
        Image(
            painter = painterResource(id = R.drawable.bg_onboarding),
            contentDescription = null, // 장식용이므로 null
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // 화면 비율에 맞춰 꽉 채우기 (잘리는 부분 발생 가능)
        )

        // 상단 컨텐츠 영역 (로고, 타이틀, 서브타이틀)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✨ 변경점 2: 보라색 라운드 박스 + 그림자 wrapper 제거
            // 새 로고(Group_481771)는 단독으로 배치 — 별도 배경 박스 불필요
            Image(
                painter = painterResource(id = R.drawable.ic_seoul_mate_logo),
                contentDescription = "로고",
                modifier = Modifier.size(80.dp), // 사이즈는 시안에 맞춰 조정 가능
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) { append("SEOUL ") }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) { append("MATE") }
                },
                fontSize = 32.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "K-뷰티·덕질·탐방·클래스\n모두 동네의 서울 메이트와 함께",
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }

        // 하단 시작하기 버튼 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showLoadingBar) {
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
                                        val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)

                                        FirebaseAuth.getInstance()
                                            .signInWithCredential(firebaseCredential)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val user = FirebaseAuth.getInstance().currentUser
                                                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                                                        if (tokenTask.isSuccessful && tokenTask.result?.token != null) {
                                                            viewModel.loginToServer(
                                                                tokenTask.result!!.token!!,
                                                                credential.id ?: ""
                                                            )
                                                        } else {
                                                            Toast.makeText(context, "토큰 발급 실패", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                } else {
                                                    Timber.tag("GoogleLogin").e(task.exception, "Firebase 인증 실패")
                                                    Toast.makeText(context, "Firebase 인증에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    }
                                } catch (e: GetCredentialException) {
                                    Timber.tag("GoogleLogin").e(e, "로그인 창 닫힘 또는 에러 발생")
                                } catch (e: Exception) {
                                    Timber.tag("GoogleLogin").e(e, "기타 에러 발생")
                                    Toast.makeText(context, "로그인 중 에러가 발생했습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("시작하기", color = Color(0xFF333333), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}