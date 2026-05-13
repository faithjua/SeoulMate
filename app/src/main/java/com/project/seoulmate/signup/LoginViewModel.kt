package com.project.seoulmate.signup

//package com.project.seoulmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.seoulmate.BuildConfig
import com.project.seoulmate.signup.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.project.seoulmate.data.remote.ApiResponse
import com.project.seoulmate.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

// 화면의 상태를 정의합니다 (대기, 로딩, 성공, 실패)
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val member: AuthResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}
@HiltViewModel
class LoginViewModel @Inject constructor(
    // 나중에 여기에 Repository나 Api를 주입받게 됨
    private val authRepository: AuthRepository, // Hilt가 NetworkModule에서 만든 걸 알아서 넣어줌
    private val userPreferences: com.project.seoulmate.data.local.UserPreferences
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    // 구글에서 받은 토큰을 임시 보관하는 변수 (회원가입시 필요하므로)
    var currentIdToken: String = ""
    var currentUserEmail: String = "" // 구글에서 받은 이메일도 임시 저장해야 회원가입화면에 넘겨줌
    //  UI에서 구글 idToken을 뽑아오면 이 함수를 호출
    fun loginToServer(idToken: String, email: String) {
        this.currentIdToken = idToken // 서버 호출 시점에 저장!
        this.currentUserEmail = email

        viewModelScope.launch {
            _loginState.value = LoginState.Loading // 로딩 뺑뺑이 시작
            Timber.tag("LoginViewModel").d("서버 통신 시작")

            try {
                // NetworkModule통한 api 통신
                val response = authRepository.login("Bearer $idToken")

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        val memberResponse = apiResponse.data // 진짜 AuthResponse 알맹이

                        // 회원 ID 저장 (기존 회원, 신규 회원 모두)
                        // 신규 회원도 백엔드에서 임시 ID를 받을 수 있으므로 저장
                        if (memberResponse.id != null) {
                            userPreferences.saveMemberId(memberResponse.id)
                            Timber.tag("LoginViewModel").d("Member ID saved: ${memberResponse.id}, isNewMember: ${memberResponse.isNewMember}")
                        } else {
                            Timber.tag("LoginViewModel").w("Member ID is null from server response")
                        }

                        Timber.tag("LoginViewModel").d("서버 통신 성공: $memberResponse")
                        _loginState.value = LoginState.Success(memberResponse)
                    } else {
                        // HTTP 200이지만 서버 로직상 에러인 경우 (예: "존재하지 않는 회원입니다")
                        _loginState.value = LoginState.Error(apiResponse.message ?: "요청 실패")
                    }
                } else {
                    // 서버가 응답은 했으나 에러인 경우 (예: 404, 500)
                    val errorBody = response.errorBody()?.string()
                    Timber.tag("LoginViewModel").e( "서버 응답 에러: ${response.code()}, 내역: $errorBody")
                    _loginState.value = LoginState.Error("서버 에러: ${response.code()}")
                }
            } catch (e: Exception) {
                Timber.tag("LoginViewModel").e(e,"네트워크 에러 발생: ${e.stackTraceToString()}")
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
    // 가입 취소 시 호출. _loginState가 Success(isNewMember=true)인 채로 남으면
    // LoginScreen의 LaunchedEffect(loginState)가 다시 발동해 Signup으로 자동 이동
    // → 가입 취소가 안 되는 무한 루프가 됨. Idle로 초기화한다.
    fun resetState() {
        _loginState.value = LoginState.Idle
        currentIdToken = ""
        currentUserEmail = ""
    }
}