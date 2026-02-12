package com.project.seoulmate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.seoulmate.R

@Composable
fun TopBar(
    onTranslateClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_seoul_mate),
            contentDescription = "Seoul Mate 로고",
            modifier = Modifier
                .width(106.dp)
                .height(20.dp)
        )

        IconButton(onClick = onTranslateClick) {
            Image(
                painter = painterResource(id = R.drawable.ic_translate),
                contentDescription = "번역",
                contentScale = ContentScale.None
            )
        }

    }
}

@Preview
@Composable
fun TopBarPreview() {
    TopBar()
}