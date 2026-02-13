package com.project.seoulmate.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.seoulmate.R

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {} // Lambda function callback for when search is clicked
) {
    // Surface is a basic building block that handles background color, shape, and elevation.
    Surface(
        modifier = modifier
            .fillMaxWidth() // Stretch to fill width
            .height(50.dp)  // Set fixed height
            .padding(horizontal = 24.dp) // Maintain consistent side padding
            .clickable(onClick = onSearchClick), // Make the whole bar clickable
        shape = RoundedCornerShape(12.dp), // Rounded corners
        color = Color(0xFFF4F4F4), // Light grey background
        // Border: 1dp thick, slightly transparent black
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f))
    ) {
        // Row arranges items horizontally: [Icon] [Text]
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Padding inside the bar
            verticalAlignment = Alignment.CenterVertically // Center items vertically
        ) {
            // 1. Search Icon
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF6C60FD), // Brand color tint
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp)) // Space between icon and text

            // 2. Placeholder Text
            Text(
                text = "어디로 가고 싶으신가요?", // "Where do you want to go?"
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    color = Color(0xFF5E5E5E) // Dark grey text color
                ),
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    SearchBar()
}