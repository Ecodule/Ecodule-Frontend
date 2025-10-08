package com.example.ecodule.ui.addtaskcontent.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ecodule.ui.addtaskcontent.common.DatePickerTextButton
import com.example.ecodule.ui.addtaskcontent.common.TimePickerTextButton

@Composable
fun DateTimeSection(
    allDay: Boolean,
    startDate: String,
    startTime: String,
    endDate: String,
    endTime: String,
    onStartDateChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.Companion.fillMaxWidth()
    ) {
        // 開始
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Text(
                "開始",
                modifier = Modifier.Companion.width(64.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.Companion.width(8.dp))
            DatePickerTextButton(
                label = "開始",
                dateText = startDate,
                onDateSelected = onStartDateChange,
                modifier = Modifier.Companion.weight(1f)
            )
            if (!allDay) {
                Spacer(modifier = Modifier.Companion.width(8.dp))
                TimePickerTextButton(
                    label = "開始",
                    timeText = startTime,
                    onTimeSelected = onStartTimeChange,
                    modifier = Modifier.Companion.width(80.dp)
                )
            }
        }
        // 終了
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Text(
                "終了",
                modifier = Modifier.Companion.width(64.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.Companion.width(8.dp))
            DatePickerTextButton(
                label = "終了",
                dateText = endDate,
                onDateSelected = onEndDateChange,
                modifier = Modifier.Companion.weight(1f)
            )
            if (!allDay) {
                Spacer(modifier = Modifier.Companion.width(8.dp))
                TimePickerTextButton(
                    label = "終了",
                    timeText = endTime,
                    onTimeSelected = onEndTimeChange,
                    modifier = Modifier.Companion.width(80.dp)
                )
            }
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Text(
        text = message,
        color = Color.Companion.Red,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}