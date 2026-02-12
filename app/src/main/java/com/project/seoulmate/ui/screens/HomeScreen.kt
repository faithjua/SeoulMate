package com.project.seoulmate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.seoulmate.ui.components.*
import com.project.seoulmate.ui.theme.SeoulMateTheme
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var selectedBottomItem by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedBottomItem,
                onItemSelected = { selectedBottomItem = it }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 메인 콘텐츠
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    // .verticalScroll(rememberScrollState())
            ) {
                // 상단바
                TopBar(
                    onTranslateClick = { /* TODO: 번역 기능 */ }
                )
                Spacer(modifier = Modifier.height(4.dp))

                // 검색창
                SearchBar(
                    onSearchClick = { /* TODO: 검색 화면으로 이동 */ }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 카테고리 그리드
                CategoryGrid(
                    onCategoryClick = { category ->
                        // TODO: 카테고리별 화면으로 이동
                        println("선택된 카테고리: ${category.name}")
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 추천 섹션 (하단 고정)
            RecommendationSection(
                modifier = Modifier.weight(0.45f),
                onSeeAllClick = { /* TODO: 전체보기 화면으로 이동 */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SeoulMateTheme {
        HomeScreen()
    }
}