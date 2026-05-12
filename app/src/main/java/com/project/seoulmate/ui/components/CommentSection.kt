package com.project.seoulmate.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.project.seoulmate.R
import com.project.seoulmate.data.model.CommentResponse
import com.project.seoulmate.util.TranslationService

private val Brand = Color(0xFF6C60FD)
private const val MAX_LEN = 500

/**
 * 댓글 영역 (목록 + 입력창)
 *
 * @param translatedById  번역된 텍스트 캐시 (commentId → 번역결과)
 * @param translatingIds  현재 번역 중인 댓글 id 집합 (스피너 노출용)
 */
@Composable
fun CommentSection(
    comments: List<CommentResponse>,
    isLoading: Boolean,
    isLoggedIn: Boolean,
    onSubmit: (content: String, isPrivate: Boolean) -> Unit,
    onDelete: (commentId: Long) -> Unit,
    onEdit: (commentId: Long, newContent: String) -> Unit,
    onTranslate: (commentId: Long, text: String) -> Unit,
    translatedById: Map<Long, String>,
    translatingIds: Set<Long>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.comment_count, comments.size),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Brand, strokeWidth = 2.dp)
                }
            }
            comments.isEmpty() -> {
                Text(
                    text = stringResource(id = R.string.comment_empty),
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            else -> {
                comments.forEach { c ->
                    CommentRow(
                        comment = c,
                        translated = translatedById[c.id],
                        isTranslating = c.id in translatingIds,
                        onTranslate = { onTranslate(c.id, c.content) },
                        onDelete = { onDelete(c.id) },
                        onEdit = { newText -> onEdit(c.id, newText) }
                    )
                    Divider(color = Color(0xFFF3F3F3), thickness = 1.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoggedIn) {
            CommentInput(onSubmit = onSubmit)
        } else {
            Text(
                text = stringResource(id = R.string.comment_login_required_hint),
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun CommentRow(
    comment: CommentResponse,
    translated: String?,
    isTranslating: Boolean,
    onTranslate: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (String) -> Unit
) {
    var editing by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        if (!comment.authorProfileImage.isNullOrBlank()) {
            AsyncImage(
                model = comment.authorProfileImage,
                contentDescription = stringResource(id = R.string.comment_profile_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.authorNickname.take(1),
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.authorNickname,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                val label: String? = when {
                    comment.isHost -> stringResource(id = R.string.comment_label_host)
                    comment.mateOrder != null -> stringResource(
                        id = R.string.comment_label_mate_order, comment.mateOrder
                    )
                    else -> null
                }
                if (label != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Brand.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = Brand,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (comment.isPrivate) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(id = R.string.comment_private),
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = stringResource(id = R.string.comment_private),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (editing) {
                CommentEditField(
                    initial = comment.content,
                    onCancel = { editing = false },
                    onSave = { newText ->
                        onEdit(newText)
                        editing = false
                    }
                )
            } else {
                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    color = if (comment.isDeleted) Color.Gray else Color(0xFF333333),
                    lineHeight = 20.sp
                )
                if (translated != null && !comment.isDeleted) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TranslatedBlock(
                        translated = translated,
                        targetLabel = TranslationService.targetLabel()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 1행: 작성시각만
                Text(
                    text = comment.createdAt,
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // 2행: 액션 버튼들 (번역/수정/삭제)
                val showTranslate = !comment.isDeleted && TranslationService.isEnabled
                if (showTranslate || comment.canEdit || comment.canDelete) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (showTranslate) {
                            TranslateInlineButton(
                                isTranslating = isTranslating,
                                isTranslated = translated != null,
                                onClick = onTranslate
                            )
                        }
                        if (comment.canEdit) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(id = R.string.comment_edit),
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable { editing = true }
                            )
                        }
                        if (comment.canDelete) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(id = R.string.comment_delete),
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable { onDelete() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentEditField(
    initial: String,
    onCancel: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initial) }

    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= MAX_LEN) text = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Brand,
                unfocusedBorderColor = Color(0xFFE0E0E0)
            ),
            supportingText = {
                Text(
                    text = stringResource(id = R.string.comment_char_count, text.length, MAX_LEN),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text(
                    text = stringResource(id = R.string.comment_cancel),
                    color = Color.Gray
                )
            }
            TextButton(
                onClick = { if (text.isNotBlank()) onSave(text.trim()) },
                enabled = text.isNotBlank()
            ) {
                Text(
                    text = stringResource(id = R.string.comment_save),
                    color = Brand,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentInput(
    onSubmit: (content: String, isPrivate: Boolean) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, Color(0xFFE0E0E0)),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= MAX_LEN) text = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            placeholder = {
                Text(
                    text = stringResource(id = R.string.comment_input_hint, MAX_LEN),
                    fontSize = 13.sp,
                    color = Color.LightGray
                )
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Brand,
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isPrivate, onCheckedChange = { isPrivate = it })
            Text(
                text = stringResource(id = R.string.comment_private_option),
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(id = R.string.comment_char_count, text.length, MAX_LEN),
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onSubmit(text.trim(), isPrivate)
                        text = ""
                        isPrivate = false
                    }
                },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Brand),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.comment_submit),
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}