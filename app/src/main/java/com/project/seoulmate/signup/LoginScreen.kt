package com.project.seoulmate.signup

import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

    //  1. ViewModel의 상태를 관찰합니다! (무한 로딩 방지의 핵심)
    val loginState by viewModel.loginState.collectAsState()

    // Firebase 토큰을 가져오는 극초반 로딩 상태
    var isFirebaseLoading by remember { mutableStateOf(true) }

    val WEB_CLIENT_ID = "103184785151-cmr2evs6iu8kau7fi7oqgu5ajig7a1mo.apps.googleusercontent.com"

    //  2. ViewModel 상태 변화에 따른 UI 처리 (네비게이션 및 에러 메시지 팝업)
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                Timber.tag("LoginScreen").d("로그인 성공! 다음 화면으로 이동")
                // 여기서 상태(isNewMember)를 보고 어디로 갈지 결정합니다!
                val member = (loginState as LoginState.Success).member
                if (member.isNewMember) {
                    onNavigateToSignup()
                } else {
                    onNavigateToHome()
                }
            }
            is LoginState.Error -> {
                // 와이파이가 끊기거나 타임아웃 발생 시 이쪽으로 옴
                val errorMessage = (loginState as LoginState.Error).message
                //지금은 개발상 에러메시지뜨지만, "네트워크오류" 이렇게 바꿀거임
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
                    isFirebaseLoading = false // 이제 로딩 제어권은 ViewModel로 넘어감
                } else {
                    // Firebase 토큰 가져오기 실패 (인터넷 끊김 등)
                    isFirebaseLoading = false
                    Toast.makeText(context, "구글 로그인 상태를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            isFirebaseLoading = false
        }
    }

    val backgroundColor = Color(0xFF6C60FD)

    //  3. 실제 로딩바를 보여줄 조건 = (Firebase 조회 중) OR (서버 API 통신 중)
    val showLoadingBar = isFirebaseLoading || loginState is LoginState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp))
                    .background(color = Color(0xFF6C60FD), shape = RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "로고",
                    contentScale = ContentScale.None
                )
            }

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
                //  4. showLoadingBar 상태에 따라 UI가 유기적으로 변함
                // 시작하기 버튼이 로딩중일땐, 로딩 이미지뜸
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
                                                    //  5. 올바른 Timber 에러 출력 문법 적용
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

/*
import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
import com.google.firebase.auth.GoogleAuthProvider
import com.project.seoulmate.R
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginTextClick: () -> Unit = {} //  하단 '로그인' 텍스트를 눌렀을 때의 동작을 위함
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val loginState by viewModel.loginState.collectAsState()

    var isFirebaseLoading by remember { mutableStateOf(true) }
    // 자동 로그인 중인지 파악하는 상태값 (이때는 버튼 대신 로딩바를 보여주기 위함)
    //var isAutoLoginChecking by remember { mutableStateOf(true) }

    val WEB_CLIENT_ID = "103184785151-cmr2evs6iu8kau7fi7oqgu5ajig7a1mo.apps.googleusercontent.com"

    //  화면이 처음 켜질 때 딱 한 번 실행되는 자동 로그인 로직
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // 1. 이미 스마트폰에 구글 로그인 기록이 남아있는 경우! (자동 로그인 진행)
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful && task.result?.token != null) {
                    val firebaseToken = task.result!!.token!!
                    val email = currentUser.email ?: "" // Firebase에서 이메일 꺼냄

                    viewModel.loginToServer(firebaseToken, email)
                    isFirebaseLoading = false // 이제 로딩 제어권은 ViewModel로 넘어감
                } else {
                    // Firebase 토큰 가져오기 실패 (인터넷 끊김 등)
                    isFirebaseLoading = false
                    Toast.makeText(context, "구글 로그인 상태를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            isFirebaseLoading = false
        }
    }
    // 시안에 맞는 배경 단색 컬러 (프로젝트 테마에 맞춰 미세조정 가능)
    val backgroundColor = Color(0xFF6C60FD)

    val showLoadingBar = isFirebaseLoading || loginState is LoginState.Loading

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
                                                    Timber.tag("GoogleLogin").d("Firebase 인증 실패",task.exception)
                                                }
                                            }
                                    }
                                } catch (e: GetCredentialException) {
                                    Timber.tag("GoogleLogin").d("로그인 창 닫힘 또는 에러").e(${e.message})
                                } catch (e: Exception) {
                                    Timber.tag("GoogleLogin").d("기타 에러: ${e.message}")
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


 */
