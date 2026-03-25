package com.project.seoulmate.signup

//package com.project.seoulmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.seoulmate.BuildConfig
import com.project.seoulmate.signup.LoginRequest
import com.project.seoulmate.signup.MemberResponse
import com.project.seoulmate.signup.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

// 화면의 상태를 정의합니다 (대기, 로딩, 성공, 실패)
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val member: MemberResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    // 구글에서 받은 토큰을 임시 보관하는 변수 (회원가입시 필요하므로)
    var currentIdToken: String = ""
    //  UI에서 구글 idToken을 뽑아오면 이 함수를 호출
    fun loginToServer(idToken: String, email: String, nickname: String) {
        this.currentIdToken = idToken // 서버 호출 시점에 저장!
        viewModelScope.launch {
            _loginState.value = LoginState.Loading // 로딩 뺑뺑이 시작
            Log.d("LoginViewModel", "서버 통신 시작: $email")

            try {
                // 백엔드로 보낼 데이터 세팅 (나중엔 SignupScreen에서 고른 값으로 덮어쓸 겁니다)
                val request = LoginRequest(
                    email = email,
                    nickname = nickname,
                    role = "TRAVELER", // 기본값
                    nationality = "KR" // 기본값
                )
                Log.d("LoginViewModel", "요청 URL: ${BuildConfig.BASE_URL}") // 실제 어디로 쏘는지 로그 확인
                // Retrofit 통신 발사!
                val response = RetrofitClient.authApi.login("Bearer $idToken", request)

                if (response.isSuccessful && response.body() != null) {
                    // 성공! (백엔드 DB에 저장됨)
                    Log.d("LoginViewModel", "서버 통신 성공: ${response.body()}")
                    _loginState.value = LoginState.Success(response.body()!!)
                } else {
                    // 서버가 응답은 했으나 에러인 경우 (예: 404, 500)
                    val errorBody = response.errorBody()?.string()
                    Log.e("LoginViewModel", "서버 응답 에러: ${response.code()}, 내역: $errorBody")
                    _loginState.value = LoginState.Error("서버 에러: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "네트워크 에러 발생: ${e.stackTraceToString()}", e)
                _loginState.value = LoginState.Error("네트워크 에러: ${e.message}")
            }
        }
    }
    fun completeSignup() {
        val currentState = _loginState.value
        if (currentState is LoginState.Success) {
            // 기존 상태를 복사하되, isNewMember만 false로 변경하여 UI를 새로고침하게 함
            _loginState.value = LoginState.Success(
                currentState.member.copy(isNewMember = false)
            )
        }
    }
}