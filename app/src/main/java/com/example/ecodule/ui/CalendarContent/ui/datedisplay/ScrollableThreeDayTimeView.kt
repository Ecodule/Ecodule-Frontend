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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.ui.HOUR_HEIGHT_DP
import com.example.ecodule.ui.CalendarContent.ui.HOURS
import com.example.ecodule.ui.CalendarContent.ui.HourBar
import com.example.ecodule.ui.CalendarContent.ui.HourBarWidth
import com.example.ecodule.ui.CalendarContent.ui.TimeGridLines
import java.time.LocalDate

private val HeaderHeight = 56.dp

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

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(HeaderHeight)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(HourBarWidth))
            days.forEach { date ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onDayClick(date.dayOfMonth) },
                    contentAlignment = Alignment.Center
                ) {
                    if (date == today) {
                        Surface(shape = CircleShape, color = Color(0xFF88C057), modifier = Modifier.size(28.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${date.dayOfMonth}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    } else {
                        Text("${date.dayOfMonth}", fontSize = 18.sp, color = Color(0xFF444444))
                    }
                }
            }
        }

        Row(Modifier.fillMaxSize()) {
            HourBar(
                scrollState = scrollState,
                labelNudgeY = (-5).dp, // 必要に応じて調整
                labelNudgeX = 5.dp
            )

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
                        TimeGridLines(verticalLines = 3, hours = HOURS)

                        Row(Modifier.fillMaxSize()) {
                            days.forEach { date ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(horizontal = 6.dp)
                                        .clickable { onDayClick(date.dayOfMonth) }
                                ) {
                                    val dayEvents = events.filter {
                                        it.startDate.dayOfMonth == date.dayOfMonth && it.startDate.monthValue == date.monthValue
                                    }
                                    dayEvents.forEach { event ->
                                        val startHour = event.startDate.hour ?: 0
                                        val endHour = event.endDate.hour ?: (startHour + 1)
                                        val durationHours = (endHour - startHour).coerceAtLeast(1)
                                        val topOffset = HOUR_HEIGHT_DP * startHour.toFloat()
                                        val eventHeight = HOUR_HEIGHT_DP * durationHours.toFloat()

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .offset(y = topOffset)
                                                .height(eventHeight)
                                                .clickable { onEventClick(event.id) },
                                            colors = CardDefaults.cardColors(
                                                containerColor = event.color.copy(alpha = 0.8f),
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(6.dp)
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