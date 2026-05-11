package com.project.seoulmate.signup

import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.seoulmate.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: com.project.seoulmate.data.local.UserPreferences
) : ViewModel() {

    // 로딩, 성공, 에러 상태를 관리 (필요시 정의)

    fun performSignup(
        idToken: String,
        email: String,
        nickname: String,
        role: String,
        nationality: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = SignupRequest(
                    email = email,
                    nickname = nickname,
                    role = role,//UI에서 Enum.name으로 대문자옴
                    nationality = nationality
                )
                val response = authRepository.signup("Bearer $idToken", request)

                // 1. 서버와 통신 자체가 성공했는지 확인 (HTTP 200 OK)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!

                    //  2. 백엔드 로직이 진짜로 성공했는지 (success == true) 한 번 더 확인!
                    if (apiResponse.success && apiResponse.data != null) {
                        // 회원 ID 저장
                        val authResponse = apiResponse.data
                        if (authResponse.id != null) {
                            userPreferences.saveMemberId(authResponse.id)
                        }
                        onSuccess()
                    } else {
                        // 통신은 됐지만 서버에서 실패를 보낸 경우 (예: 필수값 누락, DB 에러 등)
                        onError(apiResponse.message ?: "회원가입에 실패했습니다.")
                    }
                } else {
                    // 서버 통신 자체가 실패한 경우 (HTTP 400, 404, 500 등)
                    onError("서버 에러: ${response.code()}")
                }
            } catch (e: Exception) {
                // 인터넷 끊김 등의 네트워크 예외
                onError("통신 실패: ${e.message}")
            }
        }
    }
}