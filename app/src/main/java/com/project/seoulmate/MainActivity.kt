package com.project.seoulmate

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import com.project.seoulmate.signup.LoginScreen  // LoginScreen 임포트 확인
import com.project.seoulmate.signup.LoginState
import com.project.seoulmate.signup.LoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.seoulmate.signup.SignupScreen
import com.project.seoulmate.ui.screens.HomeScreen
import com.project.seoulmate.ui.theme.SeoulMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeoulMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. 테스트용으로 ViewModel 선언
                    val loginViewModel: LoginViewModel = viewModel()

                    // 2. 로그인 상태를 관찰함
                    val loginState by loginViewModel.loginState.collectAsState()

                    // 3. 상태에 따라 화면 전환 테스트
                    when (val state = loginState) {
                        is LoginState.Success -> {
                            // 서버 응답(state.member)을 분석하여 화면 결정
                            // 서버가 '신규회원여부'알려줌.  isNewMember필드 추가
                            if (state.member.isNewMember ?: true) {//서버에서 isNewMember가 null로 처리될 수 있기에, 그땐 false로
                                SignupScreen(
                                    //  ViewModel에 임시 저장해둔 구글 토큰을 가져옵니다.
                                    idToken = loginViewModel.currentIdToken,
                                    email = state.member.email,
                                    onSignupSuccess = {
                                        // 🔥 가입 성공 시 실행될 코드 State 변경
                                        Toast.makeText(this@MainActivity, "가입을 축하합니다!", Toast.LENGTH_SHORT).show()
                                        loginViewModel.completeSignup()
                                    }
                                )
                            } else {
                                // 기존 유저라면 바로 홈으로!
                                HomeScreen()
                            }
                        }
                        is LoginState.Error -> {
                            // 에러 발생 시 메시지를 보여주거나 로그인 화면 유지
                            LoginScreen(viewModel = loginViewModel)
                            // Toast나 Snackbar로 에러 메시지 표시 로직 추가 가능
                        }

                        is LoginState.Loading -> {
                            // 로딩 인디케이터 표시 (선택사항)
                            // CircularProgressIndicator()
                        }

                        else -> {
                            // 초기 상태(Idle)일 때는 로그인 화면
                            LoginScreen(viewModel = loginViewModel)
                        }
                    }
                }
            }
        }
    }
}
/*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.project.seoulmate.ui.screens.HomeScreen
import com.project.seoulmate.ui.theme.SeoulMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeoulMateTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "안녕하세요 $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SeoulMateTheme {
        Greeting("Seoul Mate")
    }
}

 */