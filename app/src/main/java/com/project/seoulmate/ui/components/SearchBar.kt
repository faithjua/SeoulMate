package com.project.seoulmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onSearchClick: () -> Unit = {}
) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp), // Slightly taller
        placeholder = {
            Text(
                text = "10,000개 이상의 서울 만남 검색",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5E5E5E), // Darker gray for readability
                )
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "검색",
                tint = Color(0xFF6C60FD), // Brand color
                modifier = Modifier.size(20.dp)
            )
        },
        shape = RoundedCornerShape(12.dp), // Less rounded
        colors = OutlinedTextFieldDefaults.colors(
            disabledPlaceholderColor = Color(0xFF5E5E5E),
            disabledLeadingIconColor = Color(0xFF6C60FD),
            disabledBorderColor = Color.Transparent,
            disabledContainerColor = Color(0xFFF5F5F7), // Very light gray background
            
            // Enabled colors if we enable it later
            focusedContainerColor = Color(0xFFF5F5F7),
            unfocusedContainerColor = Color(0xFFF5F5F7),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        singleLine = true,
        readOnly = true,
        enabled = false // Keep disabled for now as per original
    )
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    SearchBar()
}