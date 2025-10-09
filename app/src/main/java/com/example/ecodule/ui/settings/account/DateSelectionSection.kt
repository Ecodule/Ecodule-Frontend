package com.example.ecodule.ui.settings.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DateSelectionSection(
    initialYear: Int,
    initialMonth: Int, // 1..12
    initialDay: Int,
    onYearSelected: (String) -> Unit,
    onMonthSelected: (String) -> Unit,
    days: List<String>,
    onDaySelected: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        // 年
        val years = (1950..2050).map { it.toString() }
        val modifiedYears = listOf("top") + years + "bottom" // 余白用にtopとbottomをリストに追加する
        val clampedYear = initialYear.coerceIn(1950, 2050)
        ItemsPicker(
            items = modifiedYears,
            firstIndex = clampedYear - 1950,
            onItemSelected = onYearSelected
        )

        // 月（1 起点）
        val months = (1..12).map { it.toString() }
        val modifiedMonths = listOf("top") + months + "bottom"
        val clampedMonth = initialMonth.coerceIn(1, 12)
        ItemsPicker(
            items = modifiedMonths,
            firstIndex = clampedMonth - 1,
            onItemSelected = onMonthSelected
        )

        // 日
        val modifiedDays = listOf("top") + days + "bottom"
        val maxDay = days.size.coerceAtLeast(1)
        val clampedDay = initialDay.coerceIn(1, maxDay)
        ItemsPicker(
            items = modifiedDays,
            firstIndex = clampedDay - 1,
            onItemSelected = onDaySelected
        )
    }
}