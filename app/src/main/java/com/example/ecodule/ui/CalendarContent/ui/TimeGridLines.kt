package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** グリッド線（横軸：時間、縦軸：日） */
@Composable
fun TimeGridLines(verticalLines: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val hourHeight = size.height / 23f
        val colWidth = size.width / verticalLines
        // 横線（1:00～23:00）
        for (i in 0..23) {
            val y = hourHeight * i
            drawLine(
                color = Color(0xFFEEEEEE),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        // 縦線（日ごと）
        for (col in 0..verticalLines) {
            val x = colWidth * col
            drawLine(
                color = Color(0xFFEEEEEE),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}