package com.example.ecodule.ui.CalendarContent.ui.datedisplay

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.ui.CalendarContent.ui.HourBar
import com.example.ecodule.ui.CalendarContent.ui.TimeGridLines
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.util.noRippleClickable
import java.time.LocalDate

@Composable
fun ScrollableThreeDayTimeView(
    startDay: LocalDate,
    events: List<CalendarEvent>,
    onDayClick: (Int) -> Unit = {},
    onEventClick: (String) -> Unit = {}
) {
    val today = LocalDate.now()
    val days = (0..2).map { startDay.plusDays(it.toLong()) }
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
                TimeGridLines(verticalLines = 3)

                // スクロール可能なコンテンツエリア
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Row(Modifier.fillMaxHeight()) {
                        days.forEach { date ->
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .noRippleClickable { onDayClick(date.dayOfMonth) }
                            ) {
                                Column {
                                    // 日付表示エリア
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(60.dp)
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

                                    // 24時間分の予定表示エリア
                                    repeat(24) { hour ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(60.dp) // 1時間 = 60dp
                                        ) {
                                            // その日・その時間の予定をフィルタ
                                            val dayEvents = events.filter {
                                                it.startDate.dayOfMonth == date.dayOfMonth && it.startDate.monthValue == date.monthValue
                                            }

                                            val hourEvents = dayEvents.filter { event ->
                                                event.startDate.hour == hour ||
                                                        (event.startDate.hour != null && event.endDate.hour != null &&
                                                                hour >= event.startDate.hour && hour < event.endDate.hour)
                                            }

                                            // 予定を表示
                                            hourEvents.forEach { event ->
                                                if (event.startDate.hour == hour) { // 開始時間のみ表示
                                                    val eventHeight = if (event.startDate.hour != null && event.endDate.hour != null) {
                                                        ((event.endDate.hour - event.startDate.hour) * 60).dp
                                                    } else {
                                                        50.dp
                                                    }

                                                    Card(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(eventHeight.coerceAtMost(200.dp)) // 最大高さ制限
                                                            .padding(horizontal = 2.dp, vertical = 1.dp)
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
                                                                .padding(4.dp)
                                                        ) {
                                                            Text(
                                                                text = event.label,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 12.sp,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            if (event.startDate.hour != null && event.endDate.hour != null) {
                                                                Text(
                                                                    text = "${event.startDate.hour}:00-${event.endDate.hour}:00",
                                                                    fontSize = 10.sp,
                                                                    color = Color.White.copy(alpha = 0.9f)
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
            }
        }
    }
}