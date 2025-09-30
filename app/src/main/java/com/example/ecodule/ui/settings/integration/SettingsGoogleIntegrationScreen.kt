package com.example.ecodule.ui.settings.integration

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.R

@Composable
fun SettingsGoogleIntegrationScreen(
    modifier: Modifier = Modifier,
    initialGoogleLinked: Boolean = false,
    initialGoogleUserName: String = "",
    initialGoogleEmail: String = "",
    initialCalendarLinked: Boolean = false,
    onBackToSettings: () -> Unit = {},
    onGoogleAccountLink: (() -> Pair<String, String>)? = null, // 連携時にユーザー名・メール取得
    onGoogleAccountUnlink: (() -> Unit)? = null,
    onCalendarLink: (() -> Unit)? = null,
    onCalendarUnlink: (() -> Unit)? = null
) {
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var isGoogleLinked by remember { mutableStateOf(initialGoogleLinked) }
    var googleUserName by remember { mutableStateOf(initialGoogleUserName) }
    var googleEmail by remember { mutableStateOf(initialGoogleEmail) }
    var isCalendarLinked by remember { mutableStateOf(initialCalendarLinked) }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { totalDragX = 0f },
                    onDragEnd = {
                        if (totalDragX > 100f) onBackToSettings()
                        totalDragX = 0f
                    }
                ) { _, dragAmount -> totalDragX += dragAmount.x }
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
                    .clickable { onBackToSettings() }
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
                    text = "設定",
                    color = Color(0xFF007AFF),
                    fontSize = 16.sp
                )
            }
            Text(
                text = "Google カレンダーと連携",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Googleアカウント情報枠
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Googleロゴ
            Box(
                modifier = Modifier.size(44.dp).background(Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_color_icon),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (isGoogleLinked) {
                    Text(
                        text = googleUserName,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = googleEmail,
                        fontSize = 16.sp,
                        color = Color(0xFF888888)
                    )
                } else {
                    Text(
                        text = "紐づけされていません",
                        fontSize = 18.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    if (!isGoogleLinked) {
                        val userInfo = onGoogleAccountLink?.invoke() ?: ("User Name" to "testuser@gmail.com")
                        googleUserName = userInfo.first
                        googleEmail = userInfo.second
                        isGoogleLinked = true
                    } else {
                        onGoogleAccountUnlink?.invoke()
                        googleUserName = ""
                        googleEmail = ""
                        isGoogleLinked = false
                        isCalendarLinked = false // Googleアカウント解除時はカレンダーも解除
                    }
                },
                enabled = true,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = ButtonDefaults.outlinedButtonBorder,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
            ) {
                Text(
                    text = if (isGoogleLinked) "解除" else "連携",
                    fontSize = 16.sp,
                    color = if (isGoogleLinked) Color(0xFFFF3B30) else Color(0xFF007AFF),
                    fontWeight = if (isGoogleLinked) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 補足説明
        Text(
            text = "Google カレンダーと連携するにはGoogleアカウントを紐づける必要があります",
            fontSize = 14.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(start = 32.dp, top = 8.dp, end = 32.dp)
        )

        Spacer(modifier = Modifier.height(22.dp))

        // カレンダー連携枠
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Googleカレンダーロゴ
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_calendar_icon),
                    contentDescription = "Google Calendar Logo",
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))

            //カレンダー名
            Text(
                text = "Google カレンダー",
                fontSize = 19.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    if (isGoogleLinked && !isCalendarLinked) {
                        // カレンダー連携処理
                        onCalendarLink?.invoke()
                        isCalendarLinked = true
                    } else if (isGoogleLinked && isCalendarLinked) {
                        // カレンダー解除処理
                        onCalendarUnlink?.invoke()
                        isCalendarLinked = false
                    }
                },
                enabled = isGoogleLinked,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color(0xFFF0F0F0)
                ),
                border = ButtonDefaults.outlinedButtonBorder,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
            ) {
                Text(
                    text = if (isCalendarLinked) "解除" else "連携",
                    fontSize = 16.sp,
                    color = when {
                        !isGoogleLinked -> Color(0xFFBBBBBB)
                        isCalendarLinked -> Color(0xFFFF3B30)
                        else -> Color(0xFF007AFF)
                    },
                    fontWeight = if (isCalendarLinked) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}