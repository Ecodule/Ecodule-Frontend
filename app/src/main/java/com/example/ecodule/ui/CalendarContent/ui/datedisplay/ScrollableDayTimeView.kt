package com.example.ecodule.ui.CalendarContent.ui.datedisplay

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.ui.HOUR_HEIGHT_DP
import com.example.ecodule.ui.CalendarContent.ui.HOURS
import com.example.ecodule.ui.CalendarContent.ui.HourBar
import com.example.ecodule.ui.CalendarContent.ui.HourBarWidth
import com.example.ecodule.ui.CalendarContent.ui.TimeGridLines
import com.example.ecodule.ui.CalendarContent.ui.WeekNumberPillLarge
import com.example.ecodule.ui.CalendarContent.ui.calcWeekNumber
import java.time.DayOfWeek
import java.time.LocalDate

private val HeaderHeight = 56.dp

@Composable
fun ScrollableDayTimeView(
    day: LocalDate,
    events: List<CalendarEvent>,
    onDayClick: (LocalDate) -> Unit = {},
    onEventClick: (String) -> Unit = {},
    showWeekNumbers: Boolean = false,
    weekStartDay: DayOfWeek = DayOfWeek.MONDAY
) {
    val today = LocalDate.now()
    val scrollState = rememberScrollState()

    val dayEvents = events.filter {
        it.startDate.dayOfMonth == day.dayOfMonth && it.startDate.monthValue == day.monthValue
    }

    Column(Modifier.fillMaxSize()) {
        // ヘッダー（週数は時間バーの列に表示）
        Row(
            Modifier
                .fillMaxWidth()
                .height(HeaderHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(HourBarWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (showWeekNumbers) {
                    val weekNum = calcWeekNumber(day, weekStartDay)
                    WeekNumberPillLarge(
                        weekNumber = weekNum,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (day == today) {
                    Surface(shape = CircleShape, color = Color(0xFF88C057), modifier = Modifier.size(28.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${day.dayOfMonth}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${day.dayOfMonth}", color = Color(0xFF444444), fontSize = 18.sp)
                    }
                }
            }
        }

        // 本体
        Row(Modifier.fillMaxSize()) {
            // 左: 時間バー（この列に時間ラベルを表示）
            HourBar(
                scrollState = scrollState,
                labelNudgeY = (-5).dp, // 端末によって微調整
                labelNudgeX = 5.dp
            )

            // 右: グリッド + 予定（同じ scrollState で同期）
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    val contentHeight = HOUR_HEIGHT_DP * HOURS
                    Box(
                        modifier = Modifier
                            .height(contentHeight)
                            .fillMaxWidth()
                    ) {
                        TimeGridLines(verticalLines = 1, hours = HOURS)

                        dayEvents.forEach { event ->
                            val startHour = event.startDate.hour ?: 0
                            val endHour = event.endDate.hour ?: (startHour + 1)
                            val durationHours = (endHour - startHour).coerceAtLeast(1)
                            val topOffset = HOUR_HEIGHT_DP * startHour.toFloat()
                            val eventHeight = HOUR_HEIGHT_DP * durationHours.toFloat()

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .offset(y = topOffset)
                                    .height(eventHeight)
                                    .clickable { onEventClick(event.id) },
                                colors = CardDefaults.cardColors(
                                    containerColor = event.color.copy(alpha = 0.8f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Column(Modifier.padding(8.dp)) {
                                    Text(text = event.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (event.startDate.hour != null && event.endDate.hour != null) {
                                        Text(
                                            text = "${event.startDate.hour}:00 - ${event.endDate.hour}:00",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                    if (event.memo.isNotEmpty()) {
                                        Text(
                                            text = event.memo,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}