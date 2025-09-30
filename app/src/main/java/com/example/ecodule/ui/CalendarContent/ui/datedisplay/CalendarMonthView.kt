package com.example.ecodule.ui.CalendarContent.ui.datedisplay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.ui.DayCellGrid
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
    val firstDayOfWeekIndex = yearMonth.atDay(1).dayOfWeek.value % 7 // 0=日
    val daysInMonth = yearMonth.lengthOfMonth()
    val prevMonth = yearMonth.minusMonths(1)
    val prevMonthDays = prevMonth.lengthOfMonth()
    val rows = ((firstDayOfWeekIndex + daysInMonth + 6) / 7)

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
                    val isCurrentMonthCell =
                        cellIndex >= firstDayOfWeekIndex && cellIndex < firstDayOfWeekIndex + daysInMonth

                    // このセルに表示する日付（不変）
                    val dayNumber = when {
                        isCurrentMonthCell -> cellIndex - firstDayOfWeekIndex + 1
                        cellIndex < firstDayOfWeekIndex -> prevMonthDays - (firstDayOfWeekIndex - cellIndex - 1)
                        else -> cellIndex - (firstDayOfWeekIndex + daysInMonth) + 1
                    }

                    val cellModifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .let { base ->
                                if (isCurrentMonthCell) {
                                    base.noRippleClickable { onDayClick(dayNumber) }
                                } else {
                                    base
                                }
                            }

                    Box(
                        modifier = cellModifier,
                        contentAlignment = Alignment.TopCenter
                    ) {
                        when {
                            !isCurrentMonthCell && cellIndex < firstDayOfWeekIndex -> {
                                // 前月
                                Text(
                                    "$dayNumber",
                                    Modifier
                                        .fillMaxSize()
                                        .padding(top = 8.dp),
                                    color = Color(0xFFCCCCCC),
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp
                                )
                            }
                            isCurrentMonthCell -> {
                                DayCellGrid(
                                    day = dayNumber,
                                    isToday = (today.year == yearMonth.year &&
                                            today.monthValue == yearMonth.monthValue &&
                                            today.dayOfMonth == dayNumber),
                                    events = events.filter { it.startDate.dayOfMonth == dayNumber },
                                    onEventClick = onEventClick
                                )
                            }
                            else -> {
                                // 次月
                                Text(
                                    "$dayNumber",
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