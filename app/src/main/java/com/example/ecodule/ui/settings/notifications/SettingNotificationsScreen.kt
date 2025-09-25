package com.example.ecodule.ui.settings.notifications

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingNotificationsScreen(
    modifier: Modifier = Modifier,
    onBackToSettings: () -> Unit = {}
) {
    var notificationEnabled by remember { mutableStateOf(true) }
    var showBanner by remember { mutableStateOf(true) }
    var playSound by remember { mutableStateOf(true) }
    var vibration by remember { mutableStateOf(true) }
    var totalDragX by remember { mutableFloatStateOf(0f) }

    // 通知を許可がオフの場合、他の設定も強制的にオフにする
    LaunchedEffect(notificationEnabled) {
        if (!notificationEnabled) {
            showBanner = false
            playSound = false
            vibration = false
        }
    }

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
                            onBackToSettings()
                        }
                        totalDragX = 0f
                    }
                ) { _, dragAmount ->
                    totalDragX += dragAmount.x
                }
            }
    ) {
        // ヘッダー部分
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 戻るボタン（左揃え）
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
                    fontSize = 18.sp
                )
            }

            // タイトル（中央揃え）
            Text(
                text = "通知",
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

        // 通知を許可
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "通知を許可",
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = notificationEnabled,
                onCheckedChange = { notificationEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF8CC447),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFCCCCCC)
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 通知セクションタイトル
        Text(
            text = "通知",
            fontSize = 14.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)
        )

        // 通知設定項目をまとめた枠
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp)
        ) {
            // バナーを表示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "バナーを表示",
                    fontSize = 18.sp,
                    color = if (notificationEnabled) Color.Black else Color(0xFFBBBBBB),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = showBanner && notificationEnabled,
                    onCheckedChange = {
                        if (notificationEnabled) {
                            showBanner = it
                        }
                    },
                    enabled = notificationEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF8CC447),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCCCCCC),
                        disabledCheckedThumbColor = Color.White,
                        disabledCheckedTrackColor = Color(0xFFCCCCCC),
                        disabledUncheckedThumbColor = Color.White,
                        disabledUncheckedTrackColor = Color(0xFFCCCCCC)
                    )
                )
            }

            // 区切り線
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            // サウンド
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "サウンド",
                    fontSize = 18.sp,
                    color = if (notificationEnabled) Color.Black else Color(0xFFBBBBBB),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = playSound && notificationEnabled,
                    onCheckedChange = {
                        if (notificationEnabled) {
                            playSound = it
                        }
                    },
                    enabled = notificationEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF8CC447),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCCCCCC),
                        disabledCheckedThumbColor = Color.White,
                        disabledCheckedTrackColor = Color(0xFFCCCCCC),
                        disabledUncheckedThumbColor = Color.White,
                        disabledUncheckedTrackColor = Color(0xFFCCCCCC)
                    )
                )
            }

            // 区切り線
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            // バイブレーション
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "バイブレーション",
                    fontSize = 18.sp,
                    color = if (notificationEnabled) Color.Black else Color(0xFFBBBBBB),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = vibration && notificationEnabled,
                    onCheckedChange = {
                        if (notificationEnabled) {
                            vibration = it
                        }
                    },
                    enabled = notificationEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF8CC447),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCCCCCC),
                        disabledCheckedThumbColor = Color.White,
                        disabledCheckedTrackColor = Color(0xFFCCCCCC),
                        disabledUncheckedThumbColor = Color.White,
                        disabledUncheckedTrackColor = Color(0xFFCCCCCC)
                    )
                )
            }
        }
    }
}