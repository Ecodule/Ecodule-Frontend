package com.example.ecodule.ui.settings.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun DateSelectionSection(
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
        ItemsPicker(
            items = modifiedYears,
            firstIndex = currentYear - 1950,
            onItemSelected = onYearSelected
        )
        // 月
        val months = (1..12).map { it.toString() }
        val modifiedMonths = listOf("top") + months + "bottom" // 余白用にtopとbottomをリストに追加する
        ItemsPicker(
            items = modifiedMonths,
            firstIndex = currentMonth,
            onItemSelected = onMonthSelected
        )
        // 日
        val modifiedDays = listOf("top") + days + "bottom" // 余白用にtopとbottomをリストに追加する
        ItemsPicker(
            items = modifiedDays,
            firstIndex = currentDay - 1,
            onItemSelected = onDaySelected
        )
    }
}