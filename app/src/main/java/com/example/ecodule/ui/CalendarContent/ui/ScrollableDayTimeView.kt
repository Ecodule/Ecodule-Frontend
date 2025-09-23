package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.ecodule.ui.CalendarContent.ui.HourBar
import com.example.ecodule.ui.CalendarContent.ui.TimeGridLines
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.util.noRippleClickable
import java.time.LocalDate

@Composable
fun ScrollableDayTimeView(
    day: LocalDate,
    events: List<CalendarEvent>,
    onDayClick: (LocalDate) -> Unit = {},
    onEventClick: (String) -> Unit = {}
) {
    val today = LocalDate.now()
    val scrollState = rememberScrollState()

    // その日の予定をフィルタ
    val dayEvents = events.filter {
        it.startDate.dayOfMonth == day.dayOfMonth && it.startDate.monthValue == day.monthValue
    }

    Box(Modifier.fillMaxSize()) {
        Row {
            HourBar(scrollState)
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .noRippleClickable { onDayClick(day) }
            ) {
                // 時間ごとの横線
                TimeGridLines(verticalLines = 1)

                // 日付表示
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(16.dp))
                    if (day == today) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF88C057),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${day.dayOfMonth}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            "${day.dayOfMonth}",
                            fontSize = 18.sp,
                            color = Color(0xFF444444),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // 予定表示エリア
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp)
                        .verticalScroll(scrollState)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        // 24時間分のスペースを作成
                        repeat(24) { hour ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp) // 1時間 = 60dp
                            ) {
                                // この時間の予定をフィルタ
                                val hourEvents = dayEvents.filter { event ->
                                    event.startDate.hour == hour ||
                                            (event.startDate.hour != null && event.endDate.hour != null &&
                                                    hour >= event.startDate.hour && hour < event.endDate.hour)
                                }

                                // 予定を表示
                                hourEvents.forEachIndexed { index, event ->
                                    val eventHeight = if (event.startDate.hour != null && event.endDate.hour != null) {
                                        ((event.endDate.hour - event.startDate.hour) * 60).dp
                                    } else {
                                        50.dp
                                    }

                                    val topOffset = if (event.startDate.hour == hour) {
                                        0.dp
                                    } else {
                                        0.dp
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(eventHeight)
                                            .offset(y = topOffset)
                                            .padding(vertical = 2.dp, horizontal = if (index > 0) 4.dp else 0.dp)
                                            .clickable { onEventClick(event.id) },
                                        colors = CardDefaults.cardColors(
                                            containerColor = event.color.copy(alpha = 0.8f),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = event.label,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
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
                                                    color = Color.White.copy(alpha = 0.8f),
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
    }
}