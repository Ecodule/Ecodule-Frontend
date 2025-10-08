package com.example.ecodule.ui.addtaskcontent.common

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.Calendar

@Composable
fun DatePickerTextButton(
    label: String,
    dateText: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    val todayLocalDate = LocalDate.now()
    val currentYear = todayLocalDate.year

    // 表示用（既存ロジック）
    val displayDate: String = if (dateText.isNotBlank()) {
        val parts = dateText.split("/")
        if (parts.size == 3) {
            val year = parts[0].toIntOrNull() ?: currentYear
            val month = parts[1]
            val day = parts[2]
            if (year == currentYear) {
                "$month/$day"
            } else {
                "$year/$month/$day"
            }
        } else {
            dateText
        }
    } else {
        val year = todayLocalDate.year
        val month = todayLocalDate.monthValue.toString().padStart(2, '0')
        val day = todayLocalDate.dayOfMonth.toString().padStart(2, '0')
        if (year == currentYear) {
            "$month/$day"
        } else {
            "$year/$month/$day"
        }
    }

    // 初期選択日を解析
    val (initialYear, initialMonthZeroBased, initialDay) = remember(dateText) {
        parseInitialYMD(dateText, todayLocalDate)
    }

    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            val dialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val dateStr = "%04d/%02d/%02d".format(year, month + 1, dayOfMonth)
                    onDateSelected(dateStr)
                    showDatePicker = false
                },
                initialYear,
                initialMonthZeroBased,
                initialDay
            )
            dialog.setOnCancelListener { showDatePicker = false }
            dialog.show()
        }
    }

    TextButton(
        onClick = { showDatePicker = true },
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Text(
            displayDate,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * dateText から初期表示する (year, month(0-based), day) を取り出す。
 * 対応フォーマット:
 *  - yyyy/MM/dd
 *  - MM/dd  (年省略時は今年として扱う)
 * 上記以外・パース失敗時は今日の日付。
 */
private fun parseInitialYMD(dateText: String, today: LocalDate): Triple<Int, Int, Int> {
    if (dateText.isNotBlank()) {
        val parts = dateText.split("/")
        when (parts.size) {
            3 -> {
                val y = parts[0].toIntOrNull()
                val m = parts[1].toIntOrNull()
                val d = parts[2].toIntOrNull()
                if (y != null && m != null && d != null && m in 1..12 && d in 1..31) {
                    return Triple(y, m - 1, d)
                }
            }
            2 -> {
                // 年省略 (MM/dd) を想定
                val m = parts[0].toIntOrNull()
                val d = parts[1].toIntOrNull()
                if (m != null && d != null && m in 1..12 && d in 1..31) {
                    return Triple(today.year, m - 1, d)
                }
            }
        }
    }
    // fallback: 今日
    return Triple(today.year, today.monthValue - 1, today.dayOfMonth)
}