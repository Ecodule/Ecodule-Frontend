package com.example.ecodule.ui.CalendarContent.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime
import java.util.*

data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
//    val day: Int,  -> startDate.dayOfMonthに変更
    val label: String,
    val color: Color,
//    val month: Int,  -> startDate.monthValueに変更
//    val startHour: Int? = null,  -> startDate.hourに変更
//    val endHour: Int? = null,  -> endDate.hourに変更
    val startDate: LocalDateTime,  //null削除
    val endDate: LocalDateTime,  //null削除
    val category: String = "",
    val allDay: Boolean = false,
    val repeatOption: String = "しない",
    val memo: String = "",
    val notificationMinutes: Int = 10,
    val repeatGroupId: String? = null
) {
    // 時間範囲の文字列を取得
    val timeRangeText: String get() = when {
        allDay -> "終日"
        startDate.hour != null && endDate.hour != null -> "${startDate.hour}:00〜${endDate.hour}:00"
        else -> ""
    }
}