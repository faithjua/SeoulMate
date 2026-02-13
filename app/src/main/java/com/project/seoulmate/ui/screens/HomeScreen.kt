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
    // State to track which item in the bottom navigation is selected.
    // 'remember' keeps the value across recompositions (redraws).
    // 'mutableStateOf' makes it observable so UI updates when it changes.
    var selectedBottomItem by remember { mutableStateOf(0) }

    // Scaffold is a standard Material Design layout structure.
    // It provides slots for common UI elements like TopBar, BottomBar, FloatingActionButton, etc.
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedBottomItem,
                onItemSelected = { selectedBottomItem = it }
            )
        },
        containerColor = Color.White // Explicitly set background to white
    ) { paddingValues ->
        // content lambda provides 'paddingValues' to avoid overlapping with the bottom bar.
        
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the entire available screen space
                .padding(paddingValues) // Apply padding from Scaffold to avoid overlap
        ) {
            // Main Content Area
            // We use another Column for the actual page content.
            // Modifier.verticalScroll enables scrolling modification for this specific column.
            Column(
                modifier = Modifier
                    .weight(1f) // Take up all remaining vertical space above the bottom bar
                    .verticalScroll(rememberScrollState()) // Enable vertical scrolling
            ) {
                // 1. Top Bar (Logo and Translate button)
                TopBar(
                    onTranslateClick = { /* TODO: Implement Translate feature */ }
                )
                
                Spacer(modifier = Modifier.height(8.dp)) // Add vertical space

                // 2. Search Bar
                SearchBar(
                    onSearchClick = { /* TODO: Implement Search feature */ }
                )

                Spacer(modifier = Modifier.height(24.dp)) 

                // 3. Category Grid
                // Displays the grid of 12 icons. 
                // Note: The Grid itself does NOT scroll; it moves as part of this parent Column.
                CategoryGrid(
                    onCategoryClick = { category ->
                        // TODO: Handle navigation to category details
                        println("Selected: ${category.name}")
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Recommendation Section
                // Horizontal scrollable list of "Meeting" ideas.
                RecommendationSection(
                    modifier = Modifier.fillMaxWidth(),
                    onSeeAllClick = { /* TODO: Navigate to 'See All' page */ }
                )
                
                Spacer(modifier = Modifier.height(16.dp)) // Bottom padding for visual breathing room
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