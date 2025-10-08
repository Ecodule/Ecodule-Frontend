package com.example.ecodule.ui.addtaskcontent.screen

import com.example.ecodule.ui.CalendarContent.model.CalendarMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

fun addMinutesToTime(hhmm: String, minutes: Int): String {
    val parts = hhmm.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val total = (h * 60 + m + minutes).mod(24 * 60)
    val nh = total / 60
    val nm = total % 60
    return "%02d:%02d".format(nh, nm)
}

fun computeEndDateAndTime(startDate: String, startTime: String, durationMin: Int): Pair<String, String> {
    val parts = startTime.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val startTotal = h * 60 + m
    val endTotal = startTotal + durationMin
    val endTime = addMinutesToTime(startTime, durationMin)
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    var date = LocalDate.parse(startDate, dateFormatter)
    if (endTotal >= 24 * 60) {
        date = date.plusDays(1)
    }
    return date.format(dateFormatter) to endTime
}

fun snapNowToNearest30(): String {
    val now = LocalTime.now()
    var hour = now.hour
    val minute = now.minute
    val snappedMinute: Int = if (minute == 0) 0 else if (minute <= 30) 30 else {
        hour += 1
        0
    }
    if (hour >= 24) {
        hour = 23
        return "%02d:%02d".format(hour, 30)
    }
    return "%02d:%02d".format(hour, snappedMinute)
}

/**
 * カレンダーモードに基づく初期日付決定
 */
fun determineInitialDate(
    mode: CalendarMode?,
    today: LocalDate,
    baseDate: LocalDate?,
    weekStart: LocalDate?,
    threeDayStart: LocalDate?,
    shownYearMonth: YearMonth?
): LocalDate {
    if (mode == null) return today
    return when (mode) {
        CalendarMode.DAY -> baseDate ?: today
        CalendarMode.THREE_DAY -> {
            val start = threeDayStart
            if (start == null) today
            else {
                val range = listOf(start, start.plusDays(1), start.plusDays(2))
                if (today in range) today else start
            }
        }
        CalendarMode.WEEK -> {
            val ws = weekStart
            if (ws == null) today
            else {
                val end = ws.plusDays(6)
                if (!today.isBefore(ws) && !today.isAfter(end)) today else ws
            }
        }
        CalendarMode.MONTH -> {
            val ym = shownYearMonth
            if (ym == null) today
            else {
                val currentYm = YearMonth.from(today)
                if (ym == currentYm) today else ym.atDay(1)
            }
        }
        CalendarMode.SCHEDULE -> {
            val ym = shownYearMonth
            val currentYm = YearMonth.from(today)
            if (ym != null && ym != currentYm) ym.atDay(1) else today
        }
    }
}

