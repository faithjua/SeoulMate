package com.project.seoulmate.ui.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.project.seoulmate.R
import com.project.seoulmate.config.AppConfig
import com.project.seoulmate.ui.components.RecommendationCard
import com.project.seoulmate.ui.navigation.Screen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 한글 자모 분리 방지를 위한 로컬 TextFieldValue 상태
    var textFieldValue by remember { mutableStateOf(TextFieldValue(query)) }

    // ViewModel의 query가 외부에서 변경될 때(예: clearQuery) 로컬 상태 동기화
    LaunchedEffect(query) {
        if (textFieldValue.text != query) {
            textFieldValue = textFieldValue.copy(text = query)
        }
    }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 화면 첫 진입 시 자동으로 검색창 포커스
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Top Bar Section (Search Bar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.popBackStack() }
                )

                // Search Bar Input
                Box(
                    modifier = Modifier
                        .width(290.dp)
                        .height(42.dp)
                        .background(color = Color(0xFFF2F2F2), shape = RoundedCornerShape(21.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = {
                            textFieldValue = it
                            viewModel.updateQuery(it.text)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                            }
                        ),
                        decorationBox = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (textFieldValue.text.isEmpty()) {
                                        Text(
                                            text = stringResource(id = R.string.search_hint),
                                            color = Color.LightGray,
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                                if (textFieldValue.text.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color.Gray,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.clearQuery() }
                                    )
                                }
                            }
                        }
                    )
                }

                // Home Button
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Home",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. UI State Rendering
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    IdleStateContent(
                        onSearchClick = { query ->
                            viewModel.updateQuery(query)
                        }
                    )
                }
                is SearchUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6C60FD))
                    }
                }
                is SearchUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
                is SearchUiState.Success -> {
                    if (state.meetings.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(id = R.string.search_no_result),
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.meetings) { meeting ->
                                RecommendationCard(
                                    meeting = meeting,
                                    onClick = { meetingId ->
                                        // 포커스 해제 후 이동
                                        focusManager.clearFocus()
                                        navController.navigate("meeting_detail/$meetingId")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IdleStateContent(
    onSearchClick: (String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Recent Searches (개발 모드에서만 표시)
        if (!AppConfig.IS_PRODUCTION) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.search_recent),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = stringResource(id = R.string.search_clear_all),
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.clickable { /* TODO: Clear all */ }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val recentSearches = listOf("홍대 베이커리", "경복궁 야간", "맛집", "한강 피크닉")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    recentSearches.forEach { search ->
                        SearchChip(
                            text = search,
                            onClick = { onSearchClick(search) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Ad Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF6C60FD)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.search_ad_label),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.search_ad_title),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(Color(0xFF6C60FD))
            )
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(Color(0xFFE0E0E0))
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Trending Searches (개발 모드에서만 표시)
        if (!AppConfig.IS_PRODUCTION) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = stringResource(id = R.string.search_trending),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                val trendingSearches = listOf("광화문 광장", "성수동 카페거리", "인사동 쌈지길", "남산 타워", "청계천 야경")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    trendingSearches.forEach { search ->
                        SearchChip(
                            text = search,
                            onClick = { onSearchClick(search) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchChip(
    text: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .height(27.dp)
            .clickable { onClick() }
            .border(
                border = BorderStroke(1.dp, Color(0xFFEFEFEF)),
                shape = RoundedCornerShape(13.5.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove",
            tint = Color.Gray,
            modifier = Modifier
                .size(14.dp)
                .clickable { /* TODO: Remove item */ }
        )
    }
}
