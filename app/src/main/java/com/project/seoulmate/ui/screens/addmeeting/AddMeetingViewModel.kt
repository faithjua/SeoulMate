package com.project.seoulmate.ui.screens.addmeeting

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.data.model.MeetingForm
import com.project.seoulmate.data.remote.ImageApi
import com.project.seoulmate.data.repository.MeetingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * AddMeetingScreen의 폼 상태와 비즈니스 로직을 담당하는 ViewModel
 * MeetingForm 데이터 클래스 하나의 StateFlow로 통합 관리합니다
 */
@HiltViewModel
class AddMeetingViewModel @Inject constructor(
    private val repository: MeetingRepository,
    private val imageApi: ImageApi,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _formState = MutableStateFlow(MeetingForm())
    val formState: StateFlow<MeetingForm> = _formState.asStateFlow()

    // 저장/등록 결과를 UI에 전달하기 위한 이벤트 상태
    private val _uiEvent = MutableStateFlow<AddMeetingUiEvent?>(null)
    val uiEvent: StateFlow<AddMeetingUiEvent?> = _uiEvent.asStateFlow()

    // ──────────────────────────────────────────
    // 각 필드별 업데이트 함수
    // data class의 copy()를 활용해 특정 필드만 변경합니다
    // ──────────────────────────────────────────

    fun updateMeetingName(name: String) {
        if (name.length <= 10) { // 최대 10자 제한
            _formState.update { it.copy(name = name) }
        }
    }

    fun toggleCategory(category: String) {
        _formState.update { current ->
            val updated = if (current.selectedCategories.contains(category)) {
                current.selectedCategories - category
            } else {
                current.selectedCategories + category
            }
            current.copy(selectedCategories = updated)
        }
    }

    fun addCourse(course: String) {
        _formState.update { it.copy(courses = it.courses + course) }
    }

    fun updateCourseId(id: Long) {
        _formState.update { it.copy(courseId = id) }
    }

    fun removeCourse(index: Int) {
        _formState.update { current ->
            current.copy(
                courses = current.courses.filterIndexed { i, _ -> i != index }
            )
        }
    }

    fun addTimeSlot(slot: String) {
        _formState.update { it.copy(timeSlots = it.timeSlots + slot) }
    }

    fun removeTimeSlot(index: Int) {
        _formState.update { current ->
            val newTimeSlots = current.timeSlots.filterIndexed { i, _ -> i != index }
            current.copy(
                timeSlots = newTimeSlots,
                // timeSlot이 모두 삭제되면 meetDate도 초기화
                meetDate = if (newTimeSlots.isEmpty()) "" else current.meetDate
            )
        }
    }

    fun updateMeetDate(meetDate: String) {
        _formState.update { it.copy(meetDate = meetDate) }
    }

    fun updateDescription(description: String) {
        _formState.update { it.copy(description = description) }
    }

    fun updateExpectedCost(cost: String) {
        _formState.update { it.copy(expectedCost = cost) }
    }

    fun clearExpectedCost() {
        _formState.update { it.copy(expectedCost = "") }
    }

    fun updateMinMembers(min: String) {
        _formState.update { it.copy(minMembers = min) }
    }

    fun updateMaxMembers(max: String) {
        _formState.update { it.copy(maxMembers = max) }
    }

    fun updateIsRepeating(repeating: Boolean) {
        _formState.update { it.copy(isRepeating = repeating) }
    }

    // ──────────────────────────────────────────
    // 저장 / 등록 액션
    // viewModelScope: ViewModel이 살아있는 동안 유지되는 코루틴 스코프
    // ──────────────────────────────────────────

    fun saveDraft() {
        viewModelScope.launch {
            repository.saveMeetingDraft(_formState.value)
            _uiEvent.value = AddMeetingUiEvent.NavigateBack
        }
    }

    /**
     * 필수 항목 검증
     * @return 검증 실패 시 에러 메시지, 성공 시 null
     */
    private fun validateRequiredFields(): String? {
        val form = _formState.value

        return when {
            form.name.isBlank() -> "만남명을 입력해주세요"
            form.selectedCategories.isEmpty() -> "카테고리를 하나 이상 선택해주세요"
            form.courses.isEmpty() -> "코스를 추가해주세요"
            form.timeSlots.isEmpty() -> "요일/시간을 선택해주세요"
            form.expectedCost.isBlank() -> "예상 지출을 입력해주세요"
            form.minMembers.isBlank() -> "최소 인원을 입력해주세요"
            form.maxMembers.isBlank() -> "최대 인원을 입력해주세요"
            else -> {
                // 추가 검증: 최소 인원이 2명 이상인지 확인
                val minMembers = form.minMembers.toIntOrNull()
                when {
                    minMembers == null -> "최소 인원은 숫자로 입력해주세요"
                    minMembers < 2 -> "모집 최소 인원은 2명 이상이어야 합니다"
                    else -> {
                        // 최대 인원이 최소 인원보다 작지 않은지 확인
                        val maxMembers = form.maxMembers.toIntOrNull()
                        when {
                            maxMembers == null -> "최대 인원은 숫자로 입력해주세요"
                            maxMembers < minMembers -> "최대 인원은 최소 인원보다 크거나 같아야 합니다"
                            else -> null
                        }
                    }
                }
            }
        }
    }

    fun registerMeeting() {
        viewModelScope.launch {
            try {
                // 1. 필수 항목 검증
                val validationError = validateRequiredFields()
                if (validationError != null) {
                    _uiEvent.value = AddMeetingUiEvent.Error(validationError)
                    return@launch
                }

                // 2. Firebase에서 토큰 가져오기
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val idToken = tokenResult?.token

                if (idToken == null) {
                    Timber.e("Firebase token is null. User not logged in.")
                    _uiEvent.value = AddMeetingUiEvent.Error("로그인이 필요합니다")
                    return@launch
                }

                // 3. 백엔드 API로 만남 등록
                val result = repository.registerMeeting(idToken, _formState.value)

                result.onSuccess {
                    Timber.d("Meeting registered successfully: ${it.meeting.id}")
                    _uiEvent.value = AddMeetingUiEvent.NavigateBack
                }.onFailure { error ->
                    Timber.e(error, "Failed to register meeting")
                    _uiEvent.value = AddMeetingUiEvent.Error(error.message ?: "만남 등록 실패")
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during meeting registration")
                _uiEvent.value = AddMeetingUiEvent.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun onEventConsumed() {
        _uiEvent.value = null
    }

    /**
     * 이미지 업로드 (단일/다중)
     */
    fun uploadImages(uris: List<Uri>, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Firebase 토큰 획득
                val user = FirebaseAuth.getInstance().currentUser
                val token = user?.getIdToken(false)?.await()?.token

                if (token == null) {
                    Timber.e("User not logged in")
                    onComplete(false, "로그인이 필요합니다")
                    return@launch
                }

                // 2. 파일 크기 검증
                val files = uris.mapNotNull { uri ->
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val fileSize = inputStream?.available() ?: 0
                        inputStream?.close()

                        // 단일 파일 5MB 체크
                        if (fileSize > 5 * 1024 * 1024) {
                            onComplete(false, "이미지는 5MB 이하만 업로드할 수 있습니다")
                            return@launch
                        }

                        // 임시 파일로 저장
                        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        tempFile to fileSize.toLong()
                    } catch (e: Exception) {
                        Timber.e(e, "Error processing file")
                        null
                    }
                }

                // 전체 크기 20MB 체크
                val totalSize = files.sumOf { it.second }
                if (totalSize > 20 * 1024 * 1024) {
                    onComplete(false, "한 번에 업로드할 수 있는 총 용량은 20MB 이하입니다")
                    files.forEach { it.first.delete() }
                    return@launch
                }

                // 3. Multipart 생성
                val fileParts = files.map { (file, _) ->
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("file", file.name, requestFile)
                }

                val folderBody = "meetups".toRequestBody("text/plain".toMediaTypeOrNull())

                // 4. API 호출
                val imageUrls = mutableListOf<String>()

                for (filePart in fileParts) {
                    val response = imageApi.uploadImage("Bearer $token", filePart, folderBody)

                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.data?.url?.let { url ->
                            imageUrls.add(url)
                        }
                    } else {
                        val errorMsg = response.body()?.message ?: "이미지 업로드 실패"
                        Timber.e("Image upload failed: $errorMsg")
                        files.forEach { it.first.delete() }
                        onComplete(false, errorMsg)
                        return@launch
                    }
                }

                // 5. 성공 - imageUrls에 저장
                _formState.update { it.copy(imageUrls = imageUrls) }

                // 임시 파일 삭제
                files.forEach { it.first.delete() }

                Timber.d("Images uploaded successfully: ${imageUrls.size}")
                onComplete(true, null)

            } catch (e: Exception) {
                Timber.e(e, "Image upload error")
                onComplete(false, "이미지 업로드 중 오류가 발생했습니다")
            }
        }
    }
}

/**
 * UI로 전달되는 일회성 이벤트.
 * (예: 등록 성공 후 화면에서 나가기)
 */
sealed class AddMeetingUiEvent {
    object NavigateBack : AddMeetingUiEvent()
    data class Error(val message: String) : AddMeetingUiEvent()
}
