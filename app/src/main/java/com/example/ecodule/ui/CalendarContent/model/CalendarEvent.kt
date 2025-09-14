package com.example.ecodule.ui.CalendarContent.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime
import java.util.*

data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    val day: Int,
    val label: String,
    val color: Color,
    val month: Int,
    val startHour: Int? = null,
    val endHour: Int? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val category: String = "",
    val allDay: Boolean = false,
    val repeatOption: String = "しない",
    val memo: String = "",
    val notificationMinutes: Int = 10
)