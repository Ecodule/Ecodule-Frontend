package com.example.ecodule.ui.CalendarContent.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.ui.graphics.vector.ImageVector

enum class CalendarMode(val label: String, val icon: ImageVector) {
    SCHEDULE("スケジュール", Icons.Filled.ViewAgenda),
    DAY("1日", Icons.Filled.CalendarToday),
    THREE_DAY("3日", Icons.Filled.CalendarViewDay),
    WEEK("週", Icons.Filled.CalendarViewWeek),
    MONTH("月", Icons.Filled.CalendarMonth)
}