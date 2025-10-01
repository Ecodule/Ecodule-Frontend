package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields

// 既存（必要なら他画面で使用）
val WeekNumberGutterWidthDay: Dp = 28.dp
val WeekNumberColumnWidthMonth: Dp = 36.dp

fun calcWeekNumber(date: LocalDate, weekStart: DayOfWeek): Int {
    val wf = WeekFields.of(weekStart, 4)
    return date.get(wf.weekOfWeekBasedYear())
}

/** 小さめピル（主に月表示の週数カラムで使用） */
@Composable
fun WeekNumberPill(weekNumber: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(24.dp)
            .height(18.dp)
            .background(Color(0xFFE6E6E6), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$weekNumber",
            fontSize = 10.sp,
            color = Color(0xFF555555),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

/** 大きめピル（1・3・週表示のヘッダー内：時間バー列に表示） */
@Composable
fun WeekNumberPillLarge(weekNumber: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(32.dp)
            .height(22.dp)
            .background(Color(0xFFDCDCDC), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$weekNumber",
            fontSize = 12.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}