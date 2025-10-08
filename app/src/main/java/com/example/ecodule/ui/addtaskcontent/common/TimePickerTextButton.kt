package com.example.ecodule.ui.addtaskcontent.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.ui.CalendarContent.ui.WheelsTimePicker

@Composable
fun TimePickerTextButton(
    label: String,
    timeText: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val displayTime: String = timeText.ifBlank {
        if (label == "開始" || label.isBlank()) "07:00" else "08:00"
    }

    TextButton(
        onClick = { showTimePicker = true },
        modifier = modifier
            .padding(vertical = 4.dp)
    ) {
        Text(
            displayTime,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyLarge
        )
    }

    if (showTimePicker) {
        WheelsTimePicker(
            label = "${label}時刻を選択",
            currentTime = displayTime,
            onTimeChanged = { selectedTime -> onTimeSelected(selectedTime) },
            onDismissRequest = { showTimePicker = false },
            labelTextStyle = MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp),
            hourCenterBiasDp = 6.dp,
            minuteCenterBiasDp = 6.dp
        )
    }
}