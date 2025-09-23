package com.example.ecodule.ui.CalendarContent.util

import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * 指定した月の日付ごとに、繰り返し予定を表示用に展開する
 *
 * @param events 元のイベントリスト（通常＋繰り返しイベント）
 * @param yearMonth 表示対象の年月
 * @return 展開後のイベントリスト（startDate, endDateが正しく該当日にセットされたもの）
 */
fun getDisplayEventsForMonth(events: List<CalendarEvent>, yearMonth: YearMonth): List<CalendarEvent> {
    val displayed = mutableListOf<CalendarEvent>()
    val daysInMonth = yearMonth.lengthOfMonth()

    for (day in 1..daysInMonth) {
        val date = yearMonth.atDay(day)
        events.forEach { event ->
            val eventStartDate = event.startDate.toLocalDate()
            val eventEndDate = event.endDate.toLocalDate()

            when (event.repeatOption) {
                "しない" -> {
                    if (!date.isBefore(eventStartDate) && !date.isAfter(eventEndDate)) {
                        // 元のイベントの時間を保持しつつ、日付のみ変更
                        val newStartDate = LocalDateTime.of(
                            date,
                            event.startDate.toLocalTime()
                        )
                        val newEndDate = LocalDateTime.of(
                            date.plusDays(ChronoUnit.DAYS.between(eventStartDate, eventEndDate)),
                            event.endDate.toLocalTime()
                        )
                        displayed += event.copy(
                            startDate = newStartDate,
                            endDate = newEndDate
                        )
                    }
                }
                "毎日" -> {
                    if (!date.isBefore(eventStartDate)) {
                        // 元のイベントの時間を保持しつつ、日付を現在の日付に変更
                        val newStartDate = LocalDateTime.of(
                            date,
                            event.startDate.toLocalTime()
                        )
                        val newEndDate = LocalDateTime.of(
                            date.plusDays(ChronoUnit.DAYS.between(eventStartDate, eventEndDate)),
                            event.endDate.toLocalTime()
                        )
                        displayed += event.copy(
                            startDate = newStartDate,
                            endDate = newEndDate
                        )
                    }
                }
                "毎週" -> {
                    if (!date.isBefore(eventStartDate) &&
                        (date.dayOfWeek == eventStartDate.dayOfWeek)) {
                        // 元のイベントの時間を保持しつつ、日付を現在の日付に変更
                        val newStartDate = LocalDateTime.of(
                            date,
                            event.startDate.toLocalTime()
                        )
                        val newEndDate = LocalDateTime.of(
                            date.plusDays(ChronoUnit.DAYS.between(eventStartDate, eventEndDate)),
                            event.endDate.toLocalTime()
                        )
                        displayed += event.copy(
                            startDate = newStartDate,
                            endDate = newEndDate
                        )
                    }
                }
                "毎月" -> {
                    if (!date.isBefore(eventStartDate) &&
                        date.dayOfMonth == eventStartDate.dayOfMonth) {
                        // 元のイベントの時間を保持しつつ、日付を現在の日付に変更
                        val newStartDate = LocalDateTime.of(
                            date,
                            event.startDate.toLocalTime()
                        )
                        val newEndDate = LocalDateTime.of(
                            date.plusDays(ChronoUnit.DAYS.between(eventStartDate, eventEndDate)),
                            event.endDate.toLocalTime()
                        )
                        displayed += event.copy(
                            startDate = newStartDate,
                            endDate = newEndDate
                        )
                    }
                }
                "毎年" -> {
                    if (!date.isBefore(eventStartDate) &&
                        date.dayOfMonth == eventStartDate.dayOfMonth &&
                        date.month == eventStartDate.month) {
                        // 元のイベントの時間を保持しつつ、日付を現在の日付に変更
                        val newStartDate = LocalDateTime.of(
                            date,
                            event.startDate.toLocalTime()
                        )
                        val newEndDate = LocalDateTime.of(
                            date.plusDays(ChronoUnit.DAYS.between(eventStartDate, eventEndDate)),
                            event.endDate.toLocalTime()
                        )
                        displayed += event.copy(
                            startDate = newStartDate,
                            endDate = newEndDate
                        )
                    }
                }
            }
        }
    }
    return displayed
}

/**
 * 特定の日付にイベントが表示されるかどうかを判定する
 *
 * @param event チェック対象のイベント
 * @param targetDate 判定対象の日付
 * @return その日にイベントが表示される場合true
 */
fun shouldEventDisplayOnDate(event: CalendarEvent, targetDate: LocalDate): Boolean {
    val eventStartDate = event.startDate.toLocalDate()
    val eventEndDate = event.endDate.toLocalDate()

    return when (event.repeatOption) {
        "しない" -> {
            !targetDate.isBefore(eventStartDate) && !targetDate.isAfter(eventEndDate)
        }
        "毎日" -> {
            !targetDate.isBefore(eventStartDate)
        }
        "毎週" -> {
            !targetDate.isBefore(eventStartDate) && targetDate.dayOfWeek == eventStartDate.dayOfWeek
        }
        "毎月" -> {
            !targetDate.isBefore(eventStartDate) && targetDate.dayOfMonth == eventStartDate.dayOfMonth
        }
        "毎年" -> {
            !targetDate.isBefore(eventStartDate) &&
                    targetDate.dayOfMonth == eventStartDate.dayOfMonth &&
                    targetDate.month == eventStartDate.month
        }
        else -> false
    }
}

/**
 * イベントを指定した日付に対応するよう調整する
 *
 * @param event 元のイベント
 * @param targetDate 調整対象の日付
 * @return 日付が調整されたイベント
 */
fun adjustEventForDate(event: CalendarEvent, targetDate: LocalDate): CalendarEvent {
    val eventStartDate = event.startDate.toLocalDate()
    val eventEndDate = event.endDate.toLocalDate()
    val duration = ChronoUnit.DAYS.between(eventStartDate, eventEndDate)

    val newStartDate = LocalDateTime.of(targetDate, event.startDate.toLocalTime())
    val newEndDate = LocalDateTime.of(targetDate.plusDays(duration), event.endDate.toLocalTime())

    return event.copy(
        startDate = newStartDate,
        endDate = newEndDate
    )
}