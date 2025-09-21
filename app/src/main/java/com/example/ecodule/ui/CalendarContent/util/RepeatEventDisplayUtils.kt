package com.example.ecodule.ui.CalendarContent.util

import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * 指定した月の日付ごとに、繰り返し予定を表示用に展開する
 *
 * @param events 元のイベントリスト（通常＋繰り返しイベント）
 * @param yearMonth 表示対象の年月
 * @return 展開後のイベントリスト（day, monthが正しく該当日にセットされたもの）
 */
fun getDisplayEventsForMonth(events: List<CalendarEvent>, yearMonth: YearMonth): List<CalendarEvent> {
    val displayed = mutableListOf<CalendarEvent>()
    val daysInMonth = yearMonth.lengthOfMonth()

    for (day in 1..daysInMonth) {
        val date = yearMonth.atDay(day)
        events.forEach { event ->
            val startDate = event.startDate
            val endDate = event.endDate

            if (startDate == null || endDate == null) return@forEach

            val eventStart = startDate.toLocalDate()
            val eventEnd = endDate.toLocalDate()

            when (event.repeatOption) {
                "しない" -> {
                    if (!date.isBefore(eventStart) && !date.isAfter(eventEnd)) {
                        displayed += event.copy(day = day, month = yearMonth.monthValue)
                    }
                }
                "毎日" -> {
                    if (!date.isBefore(eventStart)) {
                        displayed += event.copy(day = day, month = yearMonth.monthValue)
                    }
                }
                "毎週" -> {
                    if (!date.isBefore(eventStart) &&
                        (date.dayOfWeek == eventStart.dayOfWeek)) {
                        displayed += event.copy(day = day, month = yearMonth.monthValue)
                    }
                }
                "毎月" -> {
                    if (!date.isBefore(eventStart) &&
                        date.dayOfMonth == eventStart.dayOfMonth) {
                        displayed += event.copy(day = day, month = yearMonth.monthValue)
                    }
                }
                "毎年" -> {
                    if (!date.isBefore(eventStart) &&
                        date.dayOfMonth == eventStart.dayOfMonth &&
                        date.month == eventStart.month) {
                        displayed += event.copy(day = day, month = yearMonth.monthValue)
                    }
                }
            }
        }
    }
    return displayed
}