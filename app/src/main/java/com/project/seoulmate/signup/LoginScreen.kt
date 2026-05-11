package com.project.seoulmate.signup

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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

    // ViewModel 상태 관찰
    val loginState by viewModel.loginState.collectAsState()

    // Firebase currentUser를 동기로 먼저 확인하고 초기값 결정
    //   - 비로그인 유저: 처음부터 false → 시작하기 버튼 즉시 노출
    //   - 자동로그인 유저: true → 스피너 + "로그인 정보 확인 중" 텍스트
    var isFirebaseLoading by remember {
        mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
    }

    // 시작하기 버튼 자체 로딩 상태 (CredentialManager 호출 구간 가시화)
    var isCredentialLoading by remember { mutableStateOf(false) }

    val WEB_CLIENT_ID = context.getString(R.string.default_web_client_id)

    // 에러 메시지를 미리 stringResource로 받아둠 (람다 안에서는 직접 호출 불가)
    val errStatusUnknown = stringResource(R.string.login_error_status_unknown)
    val errTokenFailed = stringResource(R.string.login_error_token_failed)
    val errFirebaseFailed = stringResource(R.string.login_error_firebase_failed)
    val errCancelled = stringResource(R.string.login_error_cancelled)
    val errGeneric = stringResource(R.string.login_error_generic)

    // ViewModel 상태 변화에 따른 UI 처리
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                Timber.tag("LoginScreen").d("로그인 성공! 다음 화면으로 이동")
                val member = (loginState as LoginState.Success).member
                if (member.isNewMember) onNavigateToSignup() else onNavigateToHome()
            }
            is LoginState.Error -> {
                isCredentialLoading = false   // 에러 시 버튼 복구
                val errorMessage = (loginState as LoginState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    // 자동 로그인 시도
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            isFirebaseLoading = false
            return@LaunchedEffect
        }
        currentUser.getIdToken(true).addOnCompleteListener { task ->
            if (task.isSuccessful && task.result?.token != null) {
                viewModel.loginToServer(task.result!!.token!!, currentUser.email ?: "")
            } else {
                Toast.makeText(context, errStatusUnknown, Toast.LENGTH_SHORT).show()
            }
            isFirebaseLoading = false
        }
    }

    // 로딩 상태별 안내 텍스트
    val loadingText: String? = when {
        isCredentialLoading -> stringResource(R.string.login_selecting_account)
        loginState is LoginState.Loading -> stringResource(R.string.login_signing_in)
        isFirebaseLoading -> stringResource(R.string.login_checking_status)
        else -> null
    }
    val showLoadingBar = loadingText != null

    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.bg_onboarding),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 상단 로고/타이틀 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_seoul_mate_logo),
                contentDescription = stringResource(R.string.logo_description),
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Normal)) { append("SEOUL ") }
                    withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append("MATE") }
                },
                fontSize = 32.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.app_tagline),
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }

        // 하단 시작하기/로딩 영역
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
                    // 스피너 + 텍스트 같이 노출
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = loadingText ?: "",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            // 클릭 즉시 로컬 로딩 ON
                            isCredentialLoading = true
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
                                        val firebaseCredential =
                                            GoogleAuthProvider.getCredential(credential.idToken, null)

                                        FirebaseAuth.getInstance()
                                            .signInWithCredential(firebaseCredential)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    FirebaseAuth.getInstance().currentUser
                                                        ?.getIdToken(true)
                                                        ?.addOnCompleteListener { tokenTask ->
                                                            if (tokenTask.isSuccessful && tokenTask.result?.token != null) {
                                                                viewModel.loginToServer(
                                                                    tokenTask.result!!.token!!,
                                                                    credential.id ?: ""
                                                                )
                                                                // ⚠️ 여기서 isCredentialLoading=false 안 함
                                                                //    loginState=Loading으로 자연스럽게 이어짐
                                                            } else {
                                                                isCredentialLoading = false
                                                                Toast.makeText(context, errTokenFailed, Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                } else {
                                                    isCredentialLoading = false
                                                    Timber.tag("GoogleLogin").e(task.exception, "Firebase 인증 실패")
                                                    Toast.makeText(context, errFirebaseFailed, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    } else {
                                        isCredentialLoading = false
                                    }
                                } catch (e: GetCredentialException) {
                                    isCredentialLoading = false
                                    Timber.tag("GoogleLogin").e(e, "로그인 창 닫힘 또는 에러 발생")
                                    Toast.makeText(context, errCancelled, Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    isCredentialLoading = false
                                    Timber.tag("GoogleLogin").e(e, "기타 에러 발생")
                                    Toast.makeText(context, errGeneric, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.login_get_started),
                            color = Color(0xFF333333),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}