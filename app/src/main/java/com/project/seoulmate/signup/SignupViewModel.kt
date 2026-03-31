package com.project.seoulmate.signup

import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.seoulmate.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
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
                val request = LoginRequest(
                    email = email,
                    nickname = nickname,
                    role = role.uppercase(),
                    nationality = nationality
                )
                val response = authRepository.login("Bearer $idToken", request)

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("서버 에러: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("통신 실패: ${e.message}")
            }
        }
    }
}