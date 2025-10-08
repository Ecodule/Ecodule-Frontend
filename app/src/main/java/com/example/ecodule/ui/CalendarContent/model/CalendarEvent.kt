package com.example.ecodule.ui.CalendarContent.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Contextual
import java.time.LocalDateTime
import java.util.*

@Serializable
data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val category: String = "",
    val description: String = "",
    val allDay: Boolean = false,
    @Serializable(with = LocalDateTimeSerializer::class)
    val startDate: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val endDate: LocalDateTime,
    val repeatOption: String = "しない",
    val memo: String = "",
    val notificationMinutes: Int = 10,
    val colorInt: Int = 0xFFB3E6FF.toInt(), // Intで保存
    val repeatGroupId: String? = null,
    @Transient
    var color: Color = Color(0xFFFFEB3B), // ← 直列化しない（@Transientで外す）
    var isCompleted: Boolean = false
) {
    val timeRangeText: String get() = when {
        allDay -> "終日"
        else -> "${startDate.hour}:00〜${endDate.hour}:00"
    }
}