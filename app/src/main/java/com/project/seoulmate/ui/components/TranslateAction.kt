package com.project.seoulmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.seoulmate.R

private val Brand = Color(0xFF6C60FD)

/**
 * 작은 인라인 번역 토글 버튼.
 *
 * - 클릭 시 onClick. 번역 중이면 스피너로 바뀐다.
 * - 라벨은 "번역" (translated == null) / "원문" (translated != null) 으로 토글.
 */
@Composable
fun TranslateInlineButton(
    isTranslating: Boolean,
    isTranslated: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = !isTranslating) { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        if (isTranslating) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.5.dp,
                color = Brand
            )
        } else {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = stringResource(id = R.string.comment_translate),
                tint = Brand,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = stringResource(
                id = if (isTranslated) R.string.comment_original
                else R.string.comment_translate
            ),
            fontSize = 11.sp,
            color = Brand
        )
    }
}

/**
 * 번역 결과를 보여주는 박스 (보라색 배경). translated가 null이면 아무것도 렌더 안 함.
 *
 * @param targetLabel 박스 좌상단 라벨 (예: "EN", "JA"). 보통 TranslationService.targetLabel() 사용.
 */
@Composable
fun TranslatedBlock(
    translated: String?,
    targetLabel: String,
    contentFontSize: TextUnit = 13.sp,
    contentColor: Color = Color(0xFF333333),
    modifier: Modifier = Modifier
) {
    if (translated == null) return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF6F5FF), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        androidx.compose.foundation.layout.Column {
            Text(
                text = targetLabel,
                fontSize = 10.sp,
                color = Brand,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = translated,
                fontSize = contentFontSize,
                color = contentColor,
                lineHeight = (contentFontSize.value + 5).sp
            )
        }
    }
}