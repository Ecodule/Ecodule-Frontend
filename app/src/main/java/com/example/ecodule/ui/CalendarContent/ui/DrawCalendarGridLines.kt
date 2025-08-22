package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DrawCalendarGridLines(
    rowCount: Int,
    colCount: Int,
    color: Color = Color(0xFFDDDDDD),
    strokeWidth: Dp = 1.dp
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellWidth = size.width / colCount
        val cellHeight = size.height / rowCount
        for (col in 1 until colCount) {
            drawLine(
                color = color,
                start = Offset(x = cellWidth * col, y = 0f),
                end = Offset(x = cellWidth * col, y = size.height),
                strokeWidth = strokeWidth.toPx()
            )
        }
        for (row in 1 until rowCount) {
            drawLine(
                color = color,
                start = Offset(x = 0f, y = cellHeight * row),
                end = Offset(x = size.width, y = cellHeight * row),
                strokeWidth = strokeWidth.toPx()
            )
        }
    }
}