package com.example.ecodule.ui.CalendarContent.util

import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * 指定した日付に表示するイベント（繰り返し含む）を展開する
 *
 * @param events 元のイベントリスト（通常＋繰り返しイベント）
 * @param targetDate 表示対象の日付
 * @return 展開後のイベントリスト（startDate, endDateが該当日にセットされたもののみ）
 */
fun getDisplayEventsForDay(
    events: List<CalendarEvent>,
    targetDate: LocalDate
): List<CalendarEvent> {
    val displayed = mutableListOf<CalendarEvent>()
    events.forEach { event ->
        val eventStartDate = event.startDate.toLocalDate()
        val eventEndDate = event.endDate.toLocalDate()

        val shouldDisplay = when (event.repeatOption) {
            "しない" -> !targetDate.isBefore(eventStartDate) && !targetDate.isAfter(eventEndDate)
            "毎日" -> !targetDate.isBefore(eventStartDate)
            "毎週" -> !targetDate.isBefore(eventStartDate) && targetDate.dayOfWeek == eventStartDate.dayOfWeek
            "毎月" -> !targetDate.isBefore(eventStartDate) && targetDate.dayOfMonth == eventStartDate.dayOfMonth
            "毎年" -> !targetDate.isBefore(eventStartDate) &&
                    targetDate.dayOfMonth == eventStartDate.dayOfMonth &&
                    targetDate.month == eventStartDate.month
            else -> false
        }
        if (shouldDisplay) {
            val duration = ChronoUnit.DAYS.between(eventStartDate, eventEndDate)
            val newStartDate = LocalDateTime.of(targetDate, event.startDate.toLocalTime())
            val newEndDate = LocalDateTime.of(targetDate.plusDays(duration), event.endDate.toLocalTime())
            displayed += event.copy(
                startDate = newStartDate,
                endDate = newEndDate
            )
        }
    }
    return displayed
}