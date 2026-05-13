package com.project.seoulmate.ui.screens.addmeeting

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.project.seoulmate.R
import com.project.seoulmate.ui.components.*
import com.project.seoulmate.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeetingScreen(
    navController: NavHostController,
    viewModel: AddMeetingViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.uiEvent.collectAsStateWithLifecycle()
    val isEditMode = viewModel.isEditMode
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // 권한 거부 Toast 메시지는 비-Composable 람다에서 stringResource를 못 부르므로
    // 여기서 미리 뽑아둔다.
    val imagePermissionDeniedMsg = stringResource(id = R.string.addmeeting_image_permission_required)
    val cameraPermissionDeniedMsg = stringResource(id = R.string.addmeeting_camera_permission_required)

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val returnedCourses by savedStateHandle?.getStateFlow<List<String>>("generated_courses", emptyList())
        ?.collectAsStateWithLifecycle(initialValue = emptyList()) ?: remember { mutableStateOf(emptyList()) }

    val returnedDescription by savedStateHandle?.getStateFlow<String>("ai_description", "")
        ?.collectAsStateWithLifecycle(initialValue = "") ?: remember { mutableStateOf("") }

    val returnedCourseId by savedStateHandle?.getStateFlow<Long?>("course_id", null)
        ?.collectAsStateWithLifecycle(initialValue = null) ?: remember { mutableStateOf(null) }

    val returnedImageUri by savedStateHandle?.getStateFlow<String?>("captured_image_uri", null)
        ?.collectAsStateWithLifecycle(initialValue = null) ?: remember { mutableStateOf(null) }

    LaunchedEffect(returnedCourses, returnedDescription, returnedCourseId, returnedImageUri) {
        if (returnedCourses.isNotEmpty()) {
            returnedCourses.forEach { viewModel.addCourse(it) }
            savedStateHandle?.remove<List<String>>("generated_courses")
        }
        if (returnedDescription.isNotBlank()) {
            viewModel.updateDescription(returnedDescription)
            savedStateHandle?.remove<String>("ai_description")
        }
        if (returnedCourseId != null) {
            viewModel.updateCourseId(returnedCourseId!!)
            savedStateHandle?.remove<Long>("course_id")
        }
        if (returnedImageUri != null) {
            val uri = android.net.Uri.parse(returnedImageUri)
            val uploadSuccessMsg = context.getString(R.string.addmeeting_image_upload_success)
            val uploadFailMsg = context.getString(R.string.addmeeting_upload_failed)
            viewModel.uploadImages(listOf(uri)) { success, errorMessage ->
                if (success) {
                    Toast.makeText(context, uploadSuccessMsg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, errorMessage ?: uploadFailMsg, Toast.LENGTH_SHORT).show()
                }
            }
            savedStateHandle?.remove<String>("captured_image_uri")
        }
    }

    // uiEvent 처리: NavigateBack은 뒤로가기, Error는 Toast 노출.
    LaunchedEffect(uiEvent) {
        when (uiEvent) {
            is AddMeetingUiEvent.NavigateBack -> {
                navController.popBackStack()
                viewModel.onEventConsumed()
            }
            is AddMeetingUiEvent.Error -> {
                Toast.makeText(
                    context,
                    (uiEvent as AddMeetingUiEvent.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.onEventConsumed()
            }
            else -> {}
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val uploadSuccessMsg = context.getString(R.string.addmeeting_image_upload_success)
            val uploadFailMsg = context.getString(R.string.addmeeting_upload_failed)
            viewModel.uploadImages(uris) { success, errorMessage ->
                if (success) {
                    Toast.makeText(context, uploadSuccessMsg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, errorMessage ?: uploadFailMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val imagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(context, imagePermissionDeniedMsg, Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            navController.navigate(Screen.Camera.route)
        } else {
            Toast.makeText(context, cameraPermissionDeniedMsg, Toast.LENGTH_SHORT).show()
        }
    }

    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = Color(0xFFDBDBDB),
        unfocusedBorderColor = Color(0xFFDBDBDB),
        focusedTextColor = Color.Gray,
        unfocusedTextColor = Color(0xFFDBDBDB),
        focusedPlaceholderColor = Color(0xFFDBDBDB),
        unfocusedPlaceholderColor = Color(0xFFDBDBDB)
    )

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Text(stringResource(id = R.string.dialog_cancel), fontSize = 16.sp, color = Color.Gray)
                }
                Button(
                    onClick = {
                        if (isEditMode) viewModel.updateMeeting() else viewModel.registerMeeting()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C60FD)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(
                                id = if (isEditMode) R.string.addmeeting_update
                                else R.string.addmeeting_submit
                            ),
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Color(0xFFF7F7F7))
                    .padding(bottom = 24.dp)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(
                                id = if (isEditMode) R.string.addmeeting_title_edit
                                else R.string.addmeeting_title_new
                            ),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.addmeeting_close)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    PhotoUploadSection(
                        imageUrls = formState.imageUrls,
                        onCameraClick = {
                            if (androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            ) {
                                navController.navigate(Screen.Camera.route)
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        onGalleryClick = {
                            val permission =
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    android.Manifest.permission.READ_MEDIA_IMAGES
                                } else {
                                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                                }
                            if (androidx.core.content.ContextCompat.checkSelfPermission(
                                    context, permission
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            ) {
                                imagePickerLauncher.launch("image/*")
                            } else {
                                imagePermissionLauncher.launch(permission)
                            }
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                FormSection(title = stringResource(id = R.string.addmeeting_label_name), required = true) {
                    OutlinedTextField(
                        value = formState.name,
                        onValueChange = { viewModel.updateMeetingName(it) },
                        placeholder = { Text(stringResource(id = R.string.addmeeting_name_placeholder)) },
                        modifier = Modifier.width(368.dp).height(59.dp),
                        singleLine = true,
                        colors = customTextFieldColors,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                FormSection(title = stringResource(id = R.string.addmeeting_label_category), required = true) {
                    CategoryTagSection(
                        categories = categories,
                        selectedCategories = formState.selectedCategories,
                        onCategoryToggle = { viewModel.toggleCategory(it) }
                    )
                }

                val dateUndecidedFallback = stringResource(id = R.string.addmeeting_date_undecided)
                val noLimitFallback = stringResource(id = R.string.addmeeting_no_limit)
                FormSection(title = stringResource(id = R.string.addmeeting_label_course), required = true) {
                    CourseSection(
                        courses = formState.courses,
                        onAddClick = {
                            val dateToPass = formState.timeSlots.firstOrNull() ?: dateUndecidedFallback
                            val categoriesToPass = formState.selectedCategories.joinToString(", ")
                            val minMem = formState.minMembers.ifBlank { noLimitFallback }
                            val maxMem = formState.maxMembers.ifBlank { noLimitFallback }
                            val cost = formState.expectedCost.ifBlank { noLimitFallback }

                            navController.currentBackStackEntry?.savedStateHandle?.apply {
                                set("ai_date", dateToPass)
                                set("ai_categories", categoriesToPass)
                                set("ai_members", "${minMem}명 ~ ${maxMem}명")
                                set("ai_cost", cost)
                            }
                            navController.navigate("add_course")
                        },
                        onRemoveCourse = { viewModel.removeCourse(it) }
                    )
                }

                FormSection(title = stringResource(id = R.string.addmeeting_label_time), required = true) {
                    TimeSlotSection(
                        timeSlots = formState.timeSlots,
                        onAddClick = { showDatePicker = true },
                        onRemoveClick = { index -> viewModel.removeTimeSlot(index) }
                    )
                }

                FormSection(title = stringResource(id = R.string.addmeeting_label_description), required = false) {
                    OutlinedTextField(
                        value = formState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        placeholder = { Text(stringResource(id = R.string.addmeeting_description_placeholder)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        colors = customTextFieldColors,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                FormSection(title = stringResource(id = R.string.addmeeting_label_cost), required = true) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = formState.expectedCost,
                            onValueChange = { viewModel.updateExpectedCost(it) },
                            placeholder = { Text(stringResource(id = R.string.addmeeting_cost_placeholder)) },
                            modifier = Modifier.weight(1f).height(59.dp),
                            trailingIcon = { Text("₩", color = Color(0xFFDBDBDB)) },
                            singleLine = true,
                            colors = customTextFieldColors,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                FormSection(title = stringResource(id = R.string.addmeeting_label_members), required = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formState.minMembers,
                                onValueChange = { viewModel.updateMinMembers(it) },
                                placeholder = { Text(stringResource(id = R.string.addmeeting_min_members_placeholder)) },
                                modifier = Modifier.weight(1f).height(59.dp),
                                singleLine = true,
                                colors = customTextFieldColors,
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = formState.maxMembers,
                                onValueChange = { viewModel.updateMaxMembers(it) },
                                placeholder = { Text(stringResource(id = R.string.addmeeting_max_members_placeholder)) },
                                modifier = Modifier.weight(1f).height(59.dp),
                                singleLine = true,
                                colors = customTextFieldColors,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.addmeeting_members_hint),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                    if (selectedDateMillis != null) showTimePicker = true
                }) { Text(stringResource(id = R.string.addmeeting_date_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(id = R.string.dialog_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    if (selectedDateMillis != null) {
                        val date = Date(selectedDateMillis!!)
                        val dateFormatter = SimpleDateFormat("M월 d일 (E)", Locale.KOREA)
                        dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
                        val dateString = dateFormatter.format(date)

                        val amPm = if (timePickerState.hour < 12) "오전" else "오후"
                        val hour12 = if (timePickerState.hour % 12 == 0) 12 else timePickerState.hour % 12
                        val minute = timePickerState.minute
                        val timeString = if (minute == 0) "$amPm ${hour12}시"
                        else "$amPm ${hour12}시 ${minute}분"

                        viewModel.addTimeSlot("$dateString $timeString")

                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = selectedDateMillis!!
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                        isoFormatter.timeZone = TimeZone.getTimeZone("UTC")
                        viewModel.updateMeetDate(isoFormatter.format(calendar.time))
                    }
                    showTimePicker = false
                }) { Text(stringResource(id = R.string.dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(id = R.string.dialog_cancel))
                }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

@Composable
fun FormSection(
    title: String,
    required: Boolean,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            if (required) {
                Text(text = " *", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        content()
    }
}