package com.project.seoulmate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
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
        },
        containerColor = Color.White // Set background to white
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Content Area (Scrollable if needed, but here fixed for simplicity)
            Column(
                modifier = Modifier
                    .weight(1f) // Fill remaining space
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Bar
                TopBar(
                    onTranslateClick = { /* TODO: Translate */ }
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                SearchBar(
                    onSearchClick = { /* TODO: Search */ }
                )

                Spacer(modifier = Modifier.height(24.dp)) // Increased spacing

                // Category Grid
                CategoryGrid(
                    onCategoryClick = { category ->
                        // TODO: Navigate
                        println("Selected: ${category.name}")
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Recommendation Section (Bottom fixed or scrollable?)
                // Based on design, it looks like it's part of the scrollable content or fills the bottom.
                // Given the "Bottom Navigation", it should scroll.
                RecommendationSection(
                    modifier = Modifier.fillMaxWidth(),
                    onSeeAllClick = { /* TODO: See All */ }
                )
                
                Spacer(modifier = Modifier.height(16.dp)) // Bottom padding
            }
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