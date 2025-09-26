package com.example.ecodule.ui.settings.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsUserNameScreen(
    modifier: Modifier = Modifier,
    currentUserName: String,
    onBackToAccount: () -> Unit = {},
    onUserNameChanged: (String) -> Unit = {}
) {
    var userName by remember { mutableStateOf(currentUserName) }
    var errorText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var totalDragX by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                    },
                    onDragEnd = {
                        // 左から右へのスワイプ（100px以上の右方向のドラッグ）を検出
                        if (totalDragX > 100f) {
                            onBackToAccount()
                        }
                        totalDragX = 0f
                    }
                ) { _, dragAmount ->
                    totalDragX += dragAmount.x
                }
            }
    ) {
        // ヘッダー
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onBackToAccount() }
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "戻る",
                    tint = Color(0xFF007AFF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "アカウント",
                    color = Color(0xFF007AFF),
                    fontSize = 14.sp
                )
            }
            Text(
                text = "ユーザー名を変更",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ラベル
        Text(
            text = "ユーザー名",
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 32.dp, bottom = 6.dp)
        )

        // ユーザー名入力欄
        OutlinedTextField(
            value = userName,
            onValueChange = {
                userName = it
                showError = false
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp)
        )

        // 記号説明
        Text(
            text = "ユーザー名に使用できる記号は . / - _ です",
            fontSize = 13.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(start = 36.dp, top = 4.dp)
        )

        // エラー表示
        if (showError) {
            Text(
                text = errorText,
                fontSize = 13.sp,
                color = Color(0xFFFF3B30),
                modifier = Modifier.padding(start = 36.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 変更ボタン
        Button(
            onClick = {
                if (UserNameValidator.validate(userName)) {
                    onUserNameChanged(userName)
                } else {
                    errorText = "ユーザー名に使用できるのは英数字と . / - _ のみです"
                    showError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8CC447),
                contentColor = Color.White
            )
        ) {
            Text("変更", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}