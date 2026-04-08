package com.project.seoulmate.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.project.seoulmate.R
import com.project.seoulmate.ui.theme.SeoulMatePrimary

@Composable
fun PhotoUploadSection(
    photoCount: Int = 0,
    onCameraClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Camera Button
        PhotoUploadButton(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = "사진",
                    tint = Color.Unspecified,
                    // contentScale = ContentScale.None
                    modifier = Modifier.size(30.dp)
                )
            },
            label = "사진",
            onClick = onCameraClick
        )

        // Gallery Button with counter
        PhotoUploadButton(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_image),
                    contentDescription = "갤러리",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(30.dp)
                )
            },
            label = "$photoCount/9",
            onClick = onGalleryClick
        )
    }
}

@Composable
fun PhotoUploadButton(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(80.dp)
            .shadow(
                elevation = 2.dp, 
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTagSection(
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit
) {
    val categories = listOf(
        "#관광", "# K-팝", "#K-뷰티", "#쇼핑",
        "#한식", "#카페", "#교통 가이드", "#숙소·지역",
        "#클래스", "#커뮤니티", "#전시·스타일", "#안전·생활"
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategories.contains(category)
            Surface(
                modifier = Modifier
                    .shadow(
                        elevation = if (isSelected) 8.dp else 5.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = if (isSelected) SeoulMatePrimary else Color.Black.copy(alpha = 0.5f),
                        ambientColor = if (isSelected) SeoulMatePrimary else Color.Black.copy(alpha = 0.5f)
                    )
                    .clickable { onCategoryToggle(category) },
                shape = RoundedCornerShape(24.dp),
                color = if (isSelected) SeoulMatePrimary else Color.White,
                border = null,
                shadowElevation = 5.dp
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = if (isSelected) Color.White else Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun CourseSection(
    courses: List<String>,
    onAddClick: () -> Unit,
    onRemoveCourse: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Display existing courses
        courses.forEachIndexed { index, course ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.width(20.dp)
                )
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = null,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = course,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        // Add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${courses.size + 1}",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.width(20.dp)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(SeoulMatePrimary, CircleShape)
                    .clip(CircleShape)
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "코스 추가",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TimeSlotSection(
    timeSlots: List<String>,
    onAddClick: () -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Display existing time slots
        timeSlots.forEach { slot ->
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = null,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = slot,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }

        // Add button
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(SeoulMatePrimary, CircleShape)
                .clip(CircleShape)
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "시간 추가",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun RepeatRegistrationSection(
    isRepeating: Boolean?,
    onRepeatChange: (Boolean) -> Unit
) {
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf("주기 설정") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Frequency setting button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .clickable { showFrequencyDialog = true }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = frequency,
                fontSize = 14.sp,
                color = if (frequency == "주기 설정") Color.Gray else Color.Black
            )
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "주기 설정",
                tint = SeoulMatePrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Radio buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onRepeatChange(true) }
            ) {
                RadioButton(
                    selected = isRepeating == true,
                    onClick = { onRepeatChange(true) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = SeoulMatePrimary
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("예", fontSize = 14.sp)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onRepeatChange(false) }
            ) {
                RadioButton(
                    selected = isRepeating == false,
                    onClick = { onRepeatChange(false) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = SeoulMatePrimary
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("아니오", fontSize = 14.sp)
            }
        }
    }

    // Frequency Dialog
    if (showFrequencyDialog) {
        AlertDialog(
            onDismissRequest = { showFrequencyDialog = false },
            title = { Text("반복 주기 설정") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("매일", "매주", "격주", "매월").forEach { option ->
                        TextButton(
                            onClick = {
                                frequency = option
                                showFrequencyDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(option, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFrequencyDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

// FlowRow implementation for older Compose versions
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(looseConstraints) }

        var currentRowWidth = 0
        var currentRowHeight = 0
        var totalHeight = 0
        val rows = mutableListOf<MutableList<Placeable>>()
        var currentRow = mutableListOf<Placeable>()

        placeables.forEach { placeable ->
            if (currentRowWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                totalHeight += currentRowHeight + 8.dp.roundToPx()
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width + 8.dp.roundToPx()
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            totalHeight += currentRowHeight
        }

        layout(constraints.maxWidth, totalHeight) {
            var yPosition = 0
            rows.forEach { row ->
                var xPosition = 0
                val rowHeight = row.maxOf { it.height }
                row.forEach { placeable ->
                    placeable.placeRelative(x = xPosition, y = yPosition)
                    xPosition += placeable.width + 8.dp.roundToPx()
                }
                yPosition += rowHeight + 8.dp.roundToPx()
            }
        }
    }
}
