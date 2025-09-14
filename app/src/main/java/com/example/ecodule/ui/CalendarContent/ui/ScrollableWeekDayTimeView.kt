package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.ui.CalendarContent.ui.HourBar
import com.example.ecodule.ui.CalendarContent.ui.TimeGridLines
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.util.noRippleClickable
import java.time.LocalDate

@Composable
fun ScrollableWeekDayTimeView(
    weekStart: LocalDate,
    events: List<CalendarEvent>,
    onDayClick: (Int) -> Unit
) {
    val today = LocalDate.now()
    val days = (0..6).map { weekStart.plusDays(it.toLong()) }
    val scrollState = rememberScrollState()
    Box(Modifier.fillMaxSize()) {
        Row {
            HourBar(scrollState)
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // 区切り線・グリッド
                TimeGridLines(verticalLines = 7)
                Row(Modifier.fillMaxHeight()) {
                    days.forEachIndexed { col, date ->
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .noRippleClickable { onDayClick(date.dayOfMonth) }
                        ) {
                            // 日付数字
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                if (date == today) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF88C057),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                "${date.dayOfMonth}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        "${date.dayOfMonth}",
                                        fontSize = 18.sp,
                                        color = Color(0xFF444444),
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        // スクロールエリア
        Box(modifier = Modifier.matchParentSize().verticalScroll(scrollState))
    }
}