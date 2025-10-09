package com.example.ecodule.ui.settings.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import kotlin.math.abs

@Composable
fun SettingsDetailsScreen(
    modifier: Modifier = Modifier,
    onBackToSettings: () -> Unit = {},
    onNavigateLicense: () -> Unit = {},
    onNavigateTerms: () -> Unit = {}
) {
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
                    tint = Color(0xFF95cf4d),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "設定",
                    color = Color(0xFF95cf4d),
                    fontSize = 14.sp
                )
            }

            // タイトル（中央揃え）
            Text(
                text = "詳細",
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

        // バージョン情報
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "バージョン",
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Beta 0.0.1",
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // オープンソース ライセンス
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .clickable { onNavigateLicense() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "オープンソース ライセンス",
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "オープンソース ライセンスへ",
                tint = Color(0xFF888888),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 利用規約
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .clickable { onNavigateTerms() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "利用規約",
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "利用規約へ",
                tint = Color(0xFF888888),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}