package com.example.ecodule.ui.CalendarContent.util

import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * 指定した週の日付ごとに、繰り返し予定を表示用に展開する
 *
 * @param events 元のイベントリスト（通常＋繰り返しイベント）
 * @param weekStart 週の開始日（例: 月曜や日曜など）
 * @return 展開後のイベントリスト（startDate, endDateが正しく該当日にセットされたもの）
 */
fun getDisplayEventsForWeek(events: List<CalendarEvent>, weekStart: LocalDate): List<CalendarEvent> {
    val displayed = mutableListOf<CalendarEvent>()
    val daysOfWeek = List(7) { weekStart.plusDays(it.toLong()) }

    for (date in daysOfWeek) {
        events.forEach { event ->
            val eventStartDate = event.startDate.toLocalDate()
            val eventEndDate = event.endDate.toLocalDate()

            val shouldDisplay = when (event.repeatOption) {
                "しない" -> {
                    !date.isBefore(eventStartDate) && !date.isAfter(eventEndDate)
                }
                "毎日" -> {
                    !date.isBefore(eventStartDate)
                }
                "毎週" -> {
                    !date.isBefore(eventStartDate) && date.dayOfWeek == eventStartDate.dayOfWeek
                }
                "毎月" -> {
                    !date.isBefore(eventStartDate) && date.dayOfMonth == eventStartDate.dayOfMonth
                }
                "毎年" -> {
                    !date.isBefore(eventStartDate) &&
                            date.dayOfMonth == eventStartDate.dayOfMonth &&
                            date.month == eventStartDate.month
                }
                else -> false
            }
            if (shouldDisplay) {
                val duration = ChronoUnit.DAYS.between(eventStartDate, eventEndDate)
                val newStartDate = LocalDateTime.of(date, event.startDate.toLocalTime())
                val newEndDate = LocalDateTime.of(date.plusDays(duration), event.endDate.toLocalTime())
                displayed += event.copy(
                    startDate = newStartDate,
                    endDate = newEndDate
                )
            }
        }
    }
    return displayed
}