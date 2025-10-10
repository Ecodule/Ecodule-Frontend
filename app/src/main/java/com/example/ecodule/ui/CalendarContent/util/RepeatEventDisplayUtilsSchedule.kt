package com.example.ecodule.ui.CalendarContent.util

import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * 指定した月の全日について、繰り返し予定を「スケジュール（リスト）表示用」に展開したイベントリストを返す
 *
 * @param events 元のイベントリスト（通常＋繰り返しイベント）
 * @param yearMonth 表示対象の年月
 * @return 展開後のイベントリスト（startDate, endDateが該当日にセットされたもののみ）
 */
fun getDisplayEventsForSchedule(
    events: List<CalendarEvent>,
    yearMonth: YearMonth
): List<CalendarEvent> {
    val displayed = mutableListOf<CalendarEvent>()
    val daysInMonth = yearMonth.lengthOfMonth()

    for (day in 1..daysInMonth) {
        val date = yearMonth.atDay(day)
        events.forEach { event ->
            val eventStartDate = event.startDate.toLocalDate()
            val eventEndDate = event.endDate.toLocalDate()

            val shouldDisplay = when (event.repeatOption) {
                "しない" -> !date.isBefore(eventStartDate) && !date.isAfter(eventEndDate)
                "毎日" -> !date.isBefore(eventStartDate)
                "毎週" -> !date.isBefore(eventStartDate) && date.dayOfWeek == eventStartDate.dayOfWeek
                "毎月" -> !date.isBefore(eventStartDate) && date.dayOfMonth == eventStartDate.dayOfMonth
                "毎年" -> !date.isBefore(eventStartDate)
                        && date.dayOfMonth == eventStartDate.dayOfMonth
                        && date.month == eventStartDate.month
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
    // 表示用に日付・時刻でソート
    return displayed.sortedWith(compareBy({ it.startDate }, { it.endDate }))
}