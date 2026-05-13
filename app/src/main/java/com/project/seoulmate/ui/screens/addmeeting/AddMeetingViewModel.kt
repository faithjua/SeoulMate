package com.project.seoulmate.ui.screens.addmeeting

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.R
import com.project.seoulmate.data.model.CategoryItem
import com.project.seoulmate.data.model.MeetingForm
import com.project.seoulmate.data.remote.ImageApi
import com.project.seoulmate.data.repository.CatalogRepository
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
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * AddMeetingScreen의 폼 상태와 비즈니스 로직을 담당하는 ViewModel
 * MeetingForm 데이터 클래스 하나의 StateFlow로 통합 관리합니다
 *
 * 수정 모드: meetingId가 있는 경우
 * 등록 모드: meetingId가 null인 경우
 */
@HiltViewModel
class AddMeetingViewModel @Inject constructor(
    private val repository: MeetingRepository,
    private val catalogRepository: CatalogRepository,
    private val imageApi: ImageApi,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Navigation argument로 받은 meetingId (수정 모드인지 확인용)
    private val meetingId: String? = savedStateHandle["meetingId"]

    // 수정 모드 여부
    val isEditMode: Boolean = meetingId != null

    private val _formState = MutableStateFlow(MeetingForm())
    val formState: StateFlow<MeetingForm> = _formState.asStateFlow()

    // 저장/등록 결과를 UI에 전달하기 위한 이벤트 상태
    private val _uiEvent = MutableStateFlow<AddMeetingUiEvent?>(null)
    val uiEvent: StateFlow<AddMeetingUiEvent?> = _uiEvent.asStateFlow()

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 카탈로그 데이터 (카테고리)
    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    init {
        // 카탈로그 데이터 로드
        loadCategories()

        // 수정 모드인 경우 기존 만남 데이터 로드
        if (isEditMode && meetingId != null) {
            loadMeetingForEdit(meetingId)
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            catalogRepository.getCategories().onSuccess { categories ->
                // TODAY 카테고리 제외 (만남 등록에서는 사용하지 않음)
                _categories.value = categories.filter { it.code != "TODAY" }
                Timber.d("Categories loaded: ${_categories.value.size} items")
            }.onFailure { error ->
                Timber.e(error, "Failed to load categories")
            }
        }
    }

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
            form.name.isBlank() -> context.getString(R.string.toast_validate_name)
            form.selectedCategories.isEmpty() -> context.getString(R.string.toast_validate_category)
            form.courses.isEmpty() -> context.getString(R.string.toast_validate_course)
            form.timeSlots.isEmpty() -> context.getString(R.string.toast_validate_time)
            form.expectedCost.isBlank() -> context.getString(R.string.toast_validate_cost)
            form.minMembers.isBlank() -> context.getString(R.string.toast_validate_min_members)
            form.maxMembers.isBlank() -> context.getString(R.string.toast_validate_max_members)
            else -> {
                val minMembers = form.minMembers.toIntOrNull()
                when {
                    minMembers == null -> context.getString(R.string.toast_validate_min_numeric)
                    minMembers < 2 -> context.getString(R.string.toast_validate_min_at_least_2)
                    else -> {
                        val maxMembers = form.maxMembers.toIntOrNull()
                        when {
                            maxMembers == null -> context.getString(R.string.toast_validate_max_numeric)
                            maxMembers < minMembers -> context.getString(R.string.toast_validate_max_gte_min)
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
                    _uiEvent.value = AddMeetingUiEvent.Error(context.getString(R.string.toast_login_required))
                    return@launch
                }

                // 3. 백엔드 API로 만남 등록
                val result = repository.registerMeeting(idToken, _formState.value)

                result.onSuccess {
                    Timber.d("Meeting registered successfully: ${it.meeting.id}")
                    _uiEvent.value = AddMeetingUiEvent.NavigateBack
                }.onFailure { error ->
                    Timber.e(error, "Failed to register meeting")
                    _uiEvent.value = AddMeetingUiEvent.Error(error.message ?: context.getString(R.string.toast_register_failed))
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during meeting registration")
                _uiEvent.value = AddMeetingUiEvent.Error(e.message ?: context.getString(R.string.toast_unknown_error))
            }
        }
    }

    /**
     * 만남 수정
     */
    fun updateMeeting() {
        if (meetingId == null) {
            _uiEvent.value = AddMeetingUiEvent.Error(context.getString(R.string.toast_no_id_to_edit))
            return
        }

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
                    _uiEvent.value = AddMeetingUiEvent.Error(context.getString(R.string.toast_login_required))
                    return@launch
                }

                _isLoading.value = true

                // 3. 백엔드 API로 만남 수정
                val result = repository.updateMeeting(idToken, meetingId, _formState.value)

                result.onSuccess {
                    Timber.d("Meeting updated successfully: ${it.meeting.id}")
                    _uiEvent.value = AddMeetingUiEvent.NavigateBack
                }.onFailure { error ->
                    Timber.e(error, "Failed to update meeting")
                    _uiEvent.value = AddMeetingUiEvent.Error(error.message ?: context.getString(R.string.toast_update_failed))
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during meeting update")
                _uiEvent.value = AddMeetingUiEvent.Error(e.message ?: context.getString(R.string.toast_unknown_error))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 수정 모드에서 기존 만남 데이터 로드
     */
    private fun loadMeetingForEdit(meetingId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Timber.d("Loading meeting for edit: $meetingId")

                val result = repository.getMeetingDetail(meetingId)

                result.onSuccess { meetingDetail ->
                    Timber.d("Meeting loaded successfully: ${meetingDetail.meeting.title}")

                    // MeetingDetail을 MeetingForm으로 변환
                    val form = MeetingForm(
                        name = meetingDetail.meeting.title,
                        description = meetingDetail.description,
                        selectedCategories = meetingDetail.meeting.tags.toSet(),
                        courses = meetingDetail.courses.map { it.name },
                        courseId = meetingDetail.courses.firstOrNull()?.let {
                            // TODO: Course ID는 서버 응답에 포함되어야 함
                            null
                        },
                        timeSlots = listOf(meetingDetail.meeting.time),
                        meetDate = meetingDetail.meeting.meetDate ?: "",
                        expectedCost = meetingDetail.meeting.price.filter { it.isDigit() },
                        minMembers = "2", // TODO: 서버 응답에서 가져와야 함
                        maxMembers = "10", // TODO: 서버 응답에서 가져와야 함
                        isRepeating = false, // TODO: 서버 응답에서 가져와야 함
                        imageUrls = meetingDetail.meeting.imageUrls,
                        ratingAvg = meetingDetail.meeting.ratingAvg
                    )

                    _formState.value = form
                }.onFailure { error ->
                    Timber.e(error, "Failed to load meeting for edit")
                    _uiEvent.value = AddMeetingUiEvent.Error(error.message ?: context.getString(R.string.toast_load_failed))
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception loading meeting for edit")
                _uiEvent.value = AddMeetingUiEvent.Error(context.getString(R.string.toast_load_generic_error))
            } finally {
                _isLoading.value = false
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
                    onComplete(false, context.getString(R.string.toast_login_required))
                    return@launch
                }

                // 2. 파일 크기 검증 및 MIME 타입 추출
                val files = uris.mapNotNull { uri ->
                    try {
                        // URI의 MIME 타입 가져오기
                        val rawMimeType = context.contentResolver.getType(uri)
                        Timber.d("=== Image Processing ===")
                        Timber.d("URI: $uri")
                        Timber.d("Raw MIME type: $rawMimeType")
                        val mimeType = normalizeMimeType(rawMimeType ?: "image/jpeg")
                        Timber.d("Normalized MIME type: $mimeType")

                        val inputStream = context.contentResolver.openInputStream(uri)
                        val fileSize = inputStream?.available() ?: 0
                        inputStream?.close()

                        // 단일 파일 5MB 체크
                        if (fileSize > 5 * 1024 * 1024) {
                            onComplete(false, context.getString(R.string.toast_image_too_large))
                            return@launch
                        }

                        // MIME 타입에 따라 파일 확장자 결정
                        val extension = when (mimeType) {
                            "image/jpeg" -> "jpg"
                            "image/png" -> "png"
                            "image/webp" -> "webp"
                            else -> "jpg"
                        }

                        // 임시 파일로 저장
                        val tempFile = File.createTempFile("upload_", ".$extension", context.cacheDir)
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        Timber.d("Temp file created: ${tempFile.name}, size: ${tempFile.length()} bytes")
                        Timber.d("=== End Image Processing ===")
                        Triple(tempFile, fileSize.toLong(), mimeType)
                    } catch (e: Exception) {
                        Timber.e(e, "Error processing file")
                        null
                    }
                }

                // 전체 크기 20MB 체크
                val totalSize = files.sumOf { it.second }
                if (totalSize > 20 * 1024 * 1024) {
                    onComplete(false, context.getString(R.string.toast_total_too_large))
                    files.forEach { it.first.delete() }
                    return@launch
                }

                // 3. Multipart 생성 (정확한 MIME 타입 사용)
                val fileParts = files.mapIndexed { index, (file, _, mimeType) ->
                    Timber.d("=== Preparing upload ${index + 1}/${files.size} ===")
                    Timber.d("File: ${file.name}")
                    Timber.d("File extension: ${file.extension}")
                    Timber.d("MIME type: $mimeType")
                    Timber.d("File size: ${file.length()} bytes")
                    val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                    Timber.d("RequestBody Content-Type: ${requestFile.contentType()}")
                    MultipartBody.Part.createFormData("file", file.name, requestFile)
                }

                val folderBody = "meetups".toRequestBody("text/plain".toMediaTypeOrNull())

                // 4. API 호출
                val imageUrls = mutableListOf<String>()

                for ((index, filePart) in fileParts.withIndex()) {
                    Timber.d("Uploading image ${index + 1}/${fileParts.size}...")
                    val response = imageApi.uploadImage("Bearer $token", filePart, folderBody)

                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.data?.url?.let { url ->
                            imageUrls.add(url)
                            Timber.d("✓ Image ${index + 1} uploaded successfully: $url")
                        }
                    } else {
                        val errorMsg = response.body()?.message ?: context.getString(R.string.toast_image_upload_failed_default)
                        val errorBody = response.errorBody()?.string()
                        Timber.e("✗ Image upload failed")
                        Timber.e("Response code: ${response.code()}")
                        Timber.e("Error message: $errorMsg")
                        Timber.e("Error body: $errorBody")
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
                onComplete(false, context.getString(R.string.toast_image_upload_generic_error))
            }
        }
    }

    /**
     * MIME 타입 정규화
     * Android에서 반환하는 비표준 MIME 타입을 표준 형식으로 변환
     * 백엔드는 image/jpeg, image/png, image/webp만 허용
     */
    private fun normalizeMimeType(mimeType: String): String {
        return when (mimeType.lowercase()) {
            "image/jpg", "image/jpeg" -> "image/jpeg"
            "image/png" -> "image/png"
            "image/webp" -> "image/webp"
            // 비표준 형식 처리
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> {
                Timber.w("Unknown MIME type: $mimeType, defaulting to image/jpeg")
                "image/jpeg"
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