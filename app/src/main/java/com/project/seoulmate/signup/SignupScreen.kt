package com.project.seoulmate.signup

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.project.seoulmate.R
import com.project.seoulmate.data.model.MemberRole
import timber.log.Timber
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    idToken: String,
    email: String,
    onSignupSuccess: () -> Unit,
    onCancelSignup: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // 디폴트값을 시스템 언어에 맞춰 동적으로 (한국어 → "대한민국", 영어 → "South Korea")
    // ⚠️ nationality 선언보다 위에 있어야 초기값으로 사용 가능
    val defaultNationality = remember {
        Locale.Builder().setRegion("KR").build().getDisplayCountry(Locale.getDefault())
    }

    var nickname by remember { mutableStateOf("soul") }
    var selectedRole by remember { mutableStateOf(MemberRole.TRAVELER) }
    var nationality by remember { mutableStateOf(defaultNationality) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFFF4F5F6)
    val primaryColor = Color(0xFF6C60FD)

    // 시스템 뒤로가기도 동일 다이얼로그 트리거 (제출 중에는 무시)
    BackHandler(enabled = !isSubmitting) { showCancelDialog = true }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = stringResource(R.string.signup_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(40.dp))

            SignupInfoField(
                label = stringResource(R.string.signup_label_email),
                value = email,
                onValueChange = {},
                readOnly = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            SignupInfoField(
                label = stringResource(R.string.signup_label_nickname),
                value = nickname,
                onValueChange = { nickname = it }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 역할 선택 드롭다운
            RoleDropdownField(
                selectedRole = selectedRole,
                onRoleSelected = { selectedRole = it }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 국적 선택 (클릭 시 바텀시트 열림)
            NationalitySelectionField(
                selectedNationality = nationality,
                onClick = { showBottomSheet = true }
            )
        }

        // 하단 고정 버튼 영역
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 취소 버튼
                Button(
                    onClick = { showCancelDialog = true },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        disabledContainerColor = Color(0xFFF5F5F5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.signup_button_cancel),
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // 완료 버튼
                Button(
                    onClick = {
                        if (isSubmitting) return@Button
                        isSubmitting = true
                        viewModel.performSignup(
                            idToken = idToken,
                            email = email,
                            nickname = nickname,
                            role = selectedRole.name,
                            nationality = nationality,
                            onSuccess = {
                                // isSubmitting = false 안 해도 됨 — 화면이 빠져나가니까
                                onSignupSuccess()
                            },
                            onError = { message ->
                                isSubmitting = false
                                Timber.tag("Signup").e("실패: $message")
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        disabledContainerColor = primaryColor.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.signup_button_submit),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    // 국적 검색 바텀시트
    if (showBottomSheet) {
        NationalityBottomSheet(
            onDismiss = { showBottomSheet = false },
            onNationalitySelected = {
                nationality = it
                showBottomSheet = false
            }
        )
    }

    // 가입 취소 확인 다이얼로그
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.signup_cancel_dialog_title)) },
            text = { Text(stringResource(R.string.signup_cancel_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        FirebaseAuth.getInstance().signOut()
                        onCancelSignup()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.signup_cancel_dialog_confirm),
                        color = Color(0xFFE53935)   // 파괴적 액션 강조
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(
                        text = stringResource(R.string.signup_cancel_dialog_dismiss),
                        color = primaryColor
                    )
                }
            },
            containerColor = Color.White
        )
    }
}


// 역할 선택 드롭다운 (Enum 사용)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdownField(selectedRole: MemberRole, onRoleSelected: (MemberRole) -> Unit) {
    val primaryColor = Color(0xFF6C60FD)
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.signup_label_role),
            color = primaryColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = selectedRole.displayName,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                MemberRole.values().forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role.displayName, color = Color(0xFF333333)) },
                        onClick = { onRoleSelected(role); expanded = false }
                    )
                }
            }
        }
    }
}

// 국적 선택 필드 (누르면 바텀시트 오픈)
@Composable
fun NationalitySelectionField(selectedNationality: String, onClick: () -> Unit) {
    val primaryColor = Color(0xFF6C60FD)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.signup_label_nationality),
            color = primaryColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = selectedNationality,
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

// 검색 가능한 국가 바텀시트
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NationalityBottomSheet(onDismiss: () -> Unit, onNationalitySelected: (String) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var searchQuery by remember { mutableStateOf("") }

    // 시스템 언어에 맞춘 국가 리스트 (한국어면 한국어, 영어면 영어)
    val countries = remember {
        val defaultLocale = Locale.getDefault()
        Locale.getISOCountries().mapNotNull { countryCode ->
            val locale = Locale.Builder().setRegion(countryCode).build()
            locale.getDisplayCountry(defaultLocale).ifBlank { null }
        }.distinct().sorted()
    }

    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) countries
        else countries.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(R.string.nationality_search_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.nationality_search_placeholder)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.nationality_search_icon_desc)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                items(filteredCountries) { country ->
                    Text(
                        text = country,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNationalitySelected(country) }
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// 시안과 동일한 하얀 배경 + 보라색 글씨 텍스트 필드
@Composable
fun SignupInfoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false
) {
    val primaryColor = Color(0xFF6C60FD)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = primaryColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                textStyle = TextStyle(
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                cursorBrush = SolidColor(primaryColor),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}