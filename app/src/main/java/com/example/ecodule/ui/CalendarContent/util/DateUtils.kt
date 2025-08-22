package com.example.ecodule.ui.CalendarContent.util

import java.time.LocalDate

// 週の開始日取得
fun getStartOfWeek(date: LocalDate): LocalDate {
    val dayOfWeek = date.dayOfWeek.value % 7 // Sunday:0, ..., Saturday:6
    return date.minusDays(dayOfWeek.toLong())
}