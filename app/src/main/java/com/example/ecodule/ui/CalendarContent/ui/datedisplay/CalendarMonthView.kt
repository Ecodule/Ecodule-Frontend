package com.example.ecodule.ui.CalendarContent.ui.datedisplay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.example.ecodule.ui.CalendarContent.ui.WeekNumberColumnWidthMonth
import com.example.ecodule.ui.CalendarContent.ui.WeekNumberPill
import com.example.ecodule.ui.CalendarContent.ui.calcWeekNumber
import com.example.ecodule.ui.CalendarContent.util.WeekConfig
import com.example.ecodule.ui.CalendarContent.util.noRippleClickable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarMonthView(
    yearMonth: YearMonth,
    events: List<CalendarEvent>,
    onDayClick: (Int) -> Unit = {},
    onEventClick: (String) -> Unit = {},
    showWeekNumbers: Boolean = false,
    weekStart: DayOfWeek = DayOfWeek.SUNDAY
) {
    val today = LocalDate.now()
    val firstDayOfWeekIndex = WeekConfig.firstDayCellIndex(yearMonth, weekStart)
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
                horizontalArrangement = Arrangement.Start
            ) {
                // 週数カラム（行ごとに表示）
                if (showWeekNumbers) {
                    // 行の最初のセルに相当する日付を 1日からの相対で求める
                    val rowStartDate = yearMonth.atDay(1)
                        .plusDays((row * 7 - firstDayOfWeekIndex).toLong())
                    val weekNum = calcWeekNumber(rowStartDate, weekStart)

                    Box(
                        modifier = Modifier
                            .width(WeekNumberColumnWidthMonth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        WeekNumberPill(weekNum)
                    }
                }

                // 日付セル 7列
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val isCurrentMonthCell =
                            cellIndex >= firstDayOfWeekIndex && cellIndex < firstDayOfWeekIndex + daysInMonth

                        // セルに表示する日付
                        val dayNumber = when {
                            isCurrentMonthCell -> cellIndex - firstDayOfWeekIndex + 1
                            cellIndex < firstDayOfWeekIndex -> prevMonthDays - (firstDayOfWeekIndex - cellIndex - 1)
                            else -> cellIndex - (firstDayOfWeekIndex + daysInMonth) + 1
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .noRippleClickable {
                                    if (isCurrentMonthCell) onDayClick(dayNumber)
                                },
                            contentAlignment = Alignment.TopCenter
                        ) {
                            when {
                                // 前月
                                !isCurrentMonthCell && cellIndex < firstDayOfWeekIndex -> {
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
                                // 当月
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
                                // 次月
                                else -> {
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
}