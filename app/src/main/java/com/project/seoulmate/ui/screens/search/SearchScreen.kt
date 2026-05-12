package com.project.seoulmate.ui.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import com.project.seoulmate.ui.util.displayCategoryName
import com.project.seoulmate.ui.util.displayCongestionLabel
import com.project.seoulmate.ui.util.displayMeetingTag
import com.project.seoulmate.ui.util.tagColor
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.project.seoulmate.data.model.Meeting

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
                    contentDescription = stringResource(id = R.string.meeting_back),
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
                                        contentDescription = stringResource(id = R.string.addcourse_clear),
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
                    contentDescription = stringResource(id = R.string.common_home),
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
                            text = stringResource(id = state.messageRes),
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
                        var selectedCategory by remember { mutableStateOf("전체") }
                        var selectedCongestion by remember { mutableStateOf("전체") }

                        LaunchedEffect(query) {
                            selectedCategory = "전체"
                            selectedCongestion = "전체"
                        }

                        val filteredMeetings = remember(state.meetings, selectedCategory, selectedCongestion) {
                            var list = state.meetings
                            if (selectedCategory != "전체" && selectedCategory != "카테고리") {
                                list = list.filter { meeting ->
                                    meeting.tags.any { tag -> tag.contains(selectedCategory) }
                                }
                            }
                            if (selectedCongestion != "전체") {
                                list = list.filter { meeting ->
                                    meeting.tags.any { tag -> tag.contains(selectedCongestion) }
                                }
                            }
                            list
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Filter Row
                            item(span = { GridItemSpan(2) }) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // options는 backend type(한글) 그대로 유지하고, 표시만 localize.
                                    val categoryLabel = stringResource(id = R.string.home_filter_category)
                                    val congestionLabel = stringResource(id = R.string.home_filter_congestion)
                                    val allLabel = stringResource(id = R.string.wishlist_filter_all)
                                    FilterChipItem(
                                        text = categoryLabel,
                                        options = listOf("전체", "관광", "K-팝", "K-뷰티", "쇼핑", "한식", "카페", "교통가이드", "숙소/지역", "클래스", "커뮤니티", "전시·스타일", "안전·생활"),
                                        selectedOption = if (selectedCategory == "전체") categoryLabel
                                        else displayCategoryName(selectedCategory),
                                        labelFor = { raw ->
                                            if (raw == "전체") allLabel else displayCategoryName(raw)
                                        },
                                        onOptionSelected = { selectedCategory = it }
                                    )

                                    FilterChipItem(
                                        text = congestionLabel,
                                        options = listOf("전체", "여유", "보통", "약간 붐빔", "붐빔"),
                                        selectedOption = if (selectedCongestion == "전체") congestionLabel
                                        else displayCongestionLabel(selectedCongestion),
                                        labelFor = { displayCongestionLabel(it) },
                                        onOptionSelected = { selectedCongestion = it }
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = CircleShape,
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { /* Filter popup */ },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Tune,
                                                contentDescription = stringResource(id = R.string.wishlist_filter_setting),
                                                tint = Color.Black,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Count Text
                            item(span = { GridItemSpan(2) }) {
                                Text(
                                    text = stringResource(id = R.string.home_meeting_count, filteredMeetings.size),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            // Grid Cards
                            items(filteredMeetings) { meeting ->
                                MeetingGridCard(
                                    meeting = meeting,
                                    onClick = {
                                        focusManager.clearFocus()
                                        navController.navigate("meeting_detail/${meeting.id}")
                                    },
                                    onFavoriteToggle = {
                                        // No-op or local state toggle if needed, safe by default
                                    }
                                )
                            }
                        }
                    }
                }
                // 기존의 LazyColumn 블록을 주석처리 또는 대체하기 위해 남은 이전의 불필요한 닫는 괄호/코드를 건너뛰도록 하기 위해 EndLine을 237로 명시하여 targetContent의 끝까지 정확하게 덮어씁니다.
                /*
                if (legacyPlaceholder == "remove_next_lines") {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.meetings) { meeting ->
                            RecommendationCard(
                                meeting = meeting,
                                onClick = { meetingId ->
                                    focusManager.clearFocus()
                                    navController.navigate("meeting_detail/$meetingId")
                                }
                            )
                        }
                    }
                }
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
                */
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
fun FilterChipItem(
    text: String,
    options: List<String>,
    selectedOption: String,
    labelFor: @Composable (String) -> String = { it },
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
            modifier = Modifier
                .height(36.dp)
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedOption,
                    fontSize = 13.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Black
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(labelFor(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MeetingGridCard(
    meeting: Meeting,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                if (meeting.imageUrl != null) {
                    AsyncImage(
                        model = meeting.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = painterResource(id = if (meeting.imageRes != 0) meeting.imageRes else R.drawable.img_recommend_1),
                        placeholder = painterResource(id = if (meeting.imageRes != 0) meeting.imageRes else R.drawable.img_recommend_1)
                    )
                } else {
                    Image(
                        painter = painterResource(id = if (meeting.imageRes != 0) meeting.imageRes else R.drawable.img_recommend_1),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (meeting.isFavorited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        tint = if (meeting.isFavorited) Color(0xFFFF6B6B) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    maxItemsInEachRow = Int.MAX_VALUE,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .fillMaxWidth(0.85f)
                ) {
                    meeting.tags.take(3).forEach { tag ->
                        Surface(
                            color = tagColor(tag),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = displayMeetingTag(tag),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = meeting.title,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = meeting.time ?: "",
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.wishlist_expected_price, meeting.price ?: ""),
                        color = Color(0xFF888888),
                        fontSize = 11.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", meeting.ratingAvg ?: 0.0),
                            color = Color(0xFF888888),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
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
            contentDescription = stringResource(id = R.string.common_remove),
            tint = Color.Gray,
            modifier = Modifier
                .size(14.dp)
                .clickable { /* TODO: Remove item */ }
        )
    }
}