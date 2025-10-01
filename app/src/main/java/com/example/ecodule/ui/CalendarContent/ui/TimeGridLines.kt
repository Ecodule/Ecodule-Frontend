package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** グリッド線（横:時間、縦:日） - スクロール領域内に配置して同期させること */
@Composable
fun TimeGridLines(
    verticalLines: Int,
    hours: Int = HOURS,
    lineColor: Color = Color(0xFFEEEEEE),
    strokeWidth: Dp = 1.dp
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val hourHeight = size.height / hours
        val colWidth = size.width / verticalLines

        // 横線（時間）: ピクセル端にスナップさせて描画
        for (i in 0..hours) {
            val y = (hourHeight * i).roundToInt().toFloat()
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth.toPx()
            )
        }
        // 縦線（日列）
        for (col in 0..verticalLines) {
            val x = (colWidth * col).roundToInt().toFloat()
            drawLine(
                color = lineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = strokeWidth.toPx()
            )
        }
    }
}