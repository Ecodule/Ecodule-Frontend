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
    modifier: Modifier = Modifier.Companion
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val todayLocalDate = LocalDate.now()
    val currentYear = todayLocalDate.year

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

    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            val dialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val dateStr = "%04d/%02d/%02d".format(year, month + 1, dayOfMonth)
                    onDateSelected(dateStr)
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dialog.setOnCancelListener { showDatePicker = false }
            dialog.show()
        }
    }
    TextButton(
        onClick = { showDatePicker = true },
        modifier = modifier
            .padding(vertical = 4.dp)
    ) {
        Text(
            displayDate,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}