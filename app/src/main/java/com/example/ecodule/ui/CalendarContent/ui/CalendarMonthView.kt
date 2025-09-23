package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.util.noRippleClickable
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarMonthView(
    yearMonth: YearMonth,
    events: List<CalendarEvent>,
    onDayClick: (Int) -> Unit = {},
    onEventClick: (String) -> Unit = {}
) {
    val today = LocalDate.now()
    val firstDayOfWeekIndex = yearMonth.atDay(1).dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val prevMonth = yearMonth.minusMonths(1)
    val prevMonthDays = prevMonth.lengthOfMonth()
    val rows = ((firstDayOfWeekIndex + daysInMonth + 6) / 7)
    var dayCounter = 1
    var nextMonthDay = 1

    Column(modifier = Modifier.fillMaxSize()) {
        for (row in 0 until rows) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .noRippleClickable {
                                if (cellIndex >= firstDayOfWeekIndex && dayCounter <= daysInMonth) {
                                    onDayClick(dayCounter)
                                }
                            },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        when {
                            cellIndex < firstDayOfWeekIndex -> {
                                Text(
                                    "${prevMonthDays - (firstDayOfWeekIndex - cellIndex - 1)}",
                                    Modifier
                                        .fillMaxSize()
                                        .padding(top = 8.dp),
                                    color = Color(0xFFCCCCCC),
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp
                                )
                            }
                            dayCounter <= daysInMonth -> {
                                DayCellGrid(
                                    day = dayCounter,
                                    isToday = (today.year == yearMonth.year && today.monthValue == yearMonth.monthValue && today.dayOfMonth == dayCounter),
                                    events = events.filter { it.startDate.dayOfMonth == dayCounter },
                                    onEventClick = onEventClick
                                )
                                dayCounter++
                            }
                            else -> {
                                Text(
                                    "${nextMonthDay++}",
                                    Modifier
                                        .fillMaxSize()
                                        .padding(top = 8.dp),
                                    color = Color(0xFFCCCCCC),
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
