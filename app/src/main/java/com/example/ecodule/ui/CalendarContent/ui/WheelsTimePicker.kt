package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Calendar

val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)

@Composable
fun WheelsTimePicker(
    label: String,
    currentTime: String,
    onTimeChanged: (String) -> Unit,
    onDismissRequest: () -> Unit,
    labelTextStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    // 時・分のセンターバイアスを外から指定できるように
    hourCenterBiasDp: Dp = 0.dp,
    minuteCenterBiasDp: Dp = 0.dp,
) {
    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        TimePickerUI(
            label = label,
            currentTime = currentTime,
            onTimeChanged = onTimeChanged,
            onDismissRequest = onDismissRequest,
            labelTextStyle = labelTextStyle,
            hourCenterBiasDp = hourCenterBiasDp,
            minuteCenterBiasDp = minuteCenterBiasDp
        )
    }
}

@Composable
fun TimePickerUI(
    label: String,
    currentTime: String,
    onTimeChanged: (String) -> Unit,
    onDismissRequest: () -> Unit,
    labelTextStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    hourCenterBiasDp: Dp = 0.dp,
    minuteCenterBiasDp: Dp = 0.dp,
) {
    // 現在の時刻を解析
    val timeParts = currentTime.split(":")
    val initialHour = timeParts.getOrNull(0)?.toIntOrNull() ?: currentHour
    val initialMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: currentMinute

    val chosenHour = remember { mutableStateOf(initialHour) }
    val chosenMinute = remember { mutableStateOf(initialMinute) }

    // 時刻が変更されたら自動的に反映
    LaunchedEffect(chosenHour.value, chosenMinute.value) {
        val timeString = "%02d:%02d".format(chosenHour.value, chosenMinute.value)
        onTimeChanged(timeString)
    }

    Card(
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 5.dp)
        ) {
            // ダイアログのラベル（スタイルを外から指定可能）
            Text(
                text = label,
                style = labelTextStyle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 時刻選択領域（分は5分刻み、かつインフィニットスクロール対応）
            TimeSelectionSection(
                initialHour = initialHour,
                initialMinute = initialMinute,
                onHourSelected = { chosenHour.value = it.toInt() },
                onMinuteSelected = { chosenMinute.value = it.toInt() },
                hourCenterBiasDp = hourCenterBiasDp,
                minuteCenterBiasDp = minuteCenterBiasDp
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}