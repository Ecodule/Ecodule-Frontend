package com.example.ecodule.ui.CalendarContent.model

import androidx.compose.ui.graphics.Color

data class CalendarEvent(
    val day: Int,
    val label: String,
    val color: Color,
    val month: Int = 7,
    val startHour: Int? = null,
    val endHour: Int? = null
)