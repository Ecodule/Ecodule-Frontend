package com.example.ecodule.ui.CalendarContent.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime
import java.util.*

class TaskViewModel {
    private val _events = mutableStateListOf<CalendarEvent>()
    val events: List<CalendarEvent> = _events

    init {
        _events.addAll(listOf(
            CalendarEvent(
                id = "1",
                day = 25,
                label = "企画進捗",
                color = Color(0xFF81C784),
                month = 9,
                startHour = 14,
                endHour = 16
            ),
            CalendarEvent(
                id = "2",
                day = 29,
                label = "買い物",
                color = Color(0xFFE57373),
                month = 9,
                startHour = 10,
                endHour = 11
            ),
            CalendarEvent(
                id = "3",
                day = 14,
                label = "ミーティング",
                color = Color(0xFFE57373),
                month = 9,
                startHour = 10,
                endHour = 11
            )
        ))
    }

    fun addEvent(
        title: String,
        category: String,
        description: String,
        allDay: Boolean,
        startDate: Date?,
        endDate: Date?,
        repeatOption: String,
        memo: String,
        notificationMinutes: Int,
        repeatGroupId: String? = null
    ) {
        val categoryColor = getCategoryColor(category)
        val startDateTime = startDate?.let {
            LocalDateTime.ofInstant(it.toInstant(), java.time.ZoneId.systemDefault())
        }
        val endDateTime = endDate?.let {
            LocalDateTime.ofInstant(it.toInstant(), java.time.ZoneId.systemDefault())
        }

        val event = CalendarEvent(
            id = UUID.randomUUID().toString(),
            day = startDateTime?.dayOfMonth ?: 1,
            label = title,
            color = categoryColor,
            month = startDateTime?.monthValue ?: 1,
            startHour = if (!allDay) startDateTime?.hour else null,
            endHour = if (!allDay) endDateTime?.hour else null,
            startDate = startDateTime,
            endDate = endDateTime,
            category = category,
            allDay = allDay,
            repeatOption = repeatOption,
            memo = memo,
            notificationMinutes = notificationMinutes,
            repeatGroupId = repeatGroupId
        )

        _events.add(event)
    }

    fun updateEvent(
        id: String,
        title: String,
        category: String,
        description: String,
        allDay: Boolean,
        startDate: Date?,
        endDate: Date?,
        repeatOption: String,
        memo: String,
        notificationMinutes: Int
    ) {
        val index = _events.indexOfFirst { it.id == id }
        if (index != -1) {
            val categoryColor = getCategoryColor(category)
            val startDateTime = startDate?.let {
                LocalDateTime.ofInstant(it.toInstant(), java.time.ZoneId.systemDefault())
            }
            val endDateTime = endDate?.let {
                LocalDateTime.ofInstant(it.toInstant(), java.time.ZoneId.systemDefault())
            }

            val updatedEvent = _events[index].copy(
                label = title,
                color = categoryColor,
                day = startDateTime?.dayOfMonth ?: _events[index].day,
                month = startDateTime?.monthValue ?: _events[index].month,
                startHour = if (!allDay && startDateTime != null) startDateTime.hour else null,
                endHour = if (!allDay && endDateTime != null) endDateTime.hour else null,
                startDate = startDateTime,
                endDate = endDateTime,
                category = category,
                allDay = allDay,
                repeatOption = repeatOption,
                memo = memo,
                notificationMinutes = notificationMinutes
            )

            _events[index] = updatedEvent
        }
    }

    fun updateEventsByRepeatGroup(
        repeatGroupId: String,
        title: String,
        category: String,
        description: String,
        allDay: Boolean,
        startDate: Date?,
        endDate: Date?,
        repeatOption: String,
        memo: String,
        notificationMinutes: Int
    ) {
        val eventsToUpdate = _events.filter { it.repeatGroupId == repeatGroupId }
        eventsToUpdate.forEach { event ->
            updateEvent(
                event.id, title, category, description, allDay,
                startDate, endDate, repeatOption, memo, notificationMinutes
            )
        }
    }

    fun deleteEvent(id: String) {
        _events.removeAll { it.id == id }
    }

    fun getEventById(id: String): CalendarEvent? {
        return _events.find { it.id == id }
    }

    private fun getCategoryColor(category: String): Color {
        return when (category) {
            "ゴミ出し" -> Color(0xFFB3E6FF)
            "通勤/通学" -> Color(0xFFFFD2C5)
            "外出" -> Color(0xFFE4EFCF)
            "買い物" -> Color(0xFFC9E4D7)
            else -> Color(0xFF81C784)
        }
    }
}