package com.example.ecodule.ui.CalendarContent.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ecodule.ui.CalendarContent.model.TaskViewModel
import com.example.ecodule.ui.CalendarContent.ui.WheelsTimePicker
import com.example.ecodule.ui.EcoduleRoute
import com.example.ecodule.ui.components.CategoryTabs
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DatePickerTextButton(
    label: String,
    dateText: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
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

@Composable
fun TimePickerTextButton(
    label: String,
    timeText: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val displayTime: String = timeText.ifBlank {
        if (label == "開始" || label.isBlank()) "07:00" else "08:00"
    }

    if (showTimePicker) {
        val parts = displayTime.split(":")
        val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 7
        val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        
        WheelsTimePicker(
            initialHour = initialHour,
            initialMinute = initialMinute,
            onTimeSelected = { hour, minute ->
                val timeStr = "%02d:%02d".format(hour, minute)
                onTimeSelected(timeStr)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            title = "${label}時刻を選択"
        )
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
}

@Composable
fun AddTaskContent(
    modifier: Modifier = Modifier,
    onSaveTask: (String, String, String, Boolean, Date?, Date?, String, String, Int) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onCancel: () -> Unit = {},
    selectedDestination: MutableState<String>,
    taskViewModel: TaskViewModel,
    editingEventId: String? = null,
    onEditComplete: () -> Unit = {}
) {
    val todayLocalDate = LocalDate.now()
    val todayString = todayLocalDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ゴミ出し") }
    var description by remember { mutableStateOf("") }
    var allDay by remember { mutableStateOf(false) }

    var startDateText by remember { mutableStateOf("") }
    var startTimeText by remember { mutableStateOf("") }
    var endDateText by remember { mutableStateOf("") }
    var endTimeText by remember { mutableStateOf("") }

    var repeatOption by remember { mutableStateOf("しない") }
    var notificationMinutes by remember { mutableStateOf(10) }
    var memo by remember { mutableStateOf("") }

    val sdfDateTime = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val sdfDate = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    var showDeleteDialog by remember { mutableStateOf(false) } // 削除確認ダイアログの状態
    val isEditing = editingEventId != null
    val titleText = if (isEditing) "タスクを編集" else "タスクを追加"
    val buttonText = if (isEditing) "更新" else "追加"

    val categories = listOf(
        "ゴミ出し" to Color(0xFFB3E6FF),
        "通勤/通学" to Color(0xFFFFD2C5),
        "外出" to Color(0xFFE4EFCF),
        "買い物" to Color(0xFFC9E4D7)
    )

    // 編集時のデータ読み込み
    LaunchedEffect(editingEventId) {
        if (editingEventId != null) {
            val event = taskViewModel.getEventById(editingEventId)
            event?.let {
                title = it.label
                category = it.category
                allDay = it.allDay
                it.startDate?.let { date ->
                    val actualDate = when (date) {
                        is LocalDateTime -> Date.from(date.atZone(ZoneId.systemDefault()).toInstant())
                        is Date -> date
                        else -> null
                    }
                    if (actualDate != null) {
                        val cal = Calendar.getInstance()
                        cal.time = actualDate
                        startDateText = "%04d/%02d/%02d".format(
                            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
                        )
                        startTimeText = "%02d:%02d".format(
                            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)
                        )
                    }
                }
                it.endDate?.let { date ->
                    val actualDate = when (date) {
                        is LocalDateTime -> Date.from(date.atZone(ZoneId.systemDefault()).toInstant())
                        is Date -> date
                        else -> null
                    }
                    if (actualDate != null) {
                        val cal = Calendar.getInstance()
                        cal.time = actualDate
                        endDateText = "%04d/%02d/%02d".format(
                            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
                        )
                        endTimeText = "%02d:%02d".format(
                            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)
                        )
                    }
                }
                repeatOption = it.repeatOption
                memo = it.memo
                notificationMinutes = it.notificationMinutes
            }
        }
    }

    val actualStartDate = startDateText.ifBlank { todayString }
    val actualStartTime = startTimeText.ifBlank { "07:00" }
    val actualEndDate = endDateText.ifBlank { todayString }
    val actualEndTime = endTimeText.ifBlank { "08:00" }

    val startDate: Date? = try {
        if (allDay) sdfDate.parse(actualStartDate)
        else if (actualStartDate.isNotBlank() && actualStartTime.isNotBlank())
            sdfDateTime.parse("${actualStartDate} ${actualStartTime}")
        else null
    } catch (e: Exception) { null }
    val endDate: Date? = try {
        if (allDay) sdfDate.parse(actualEndDate)
        else if (actualEndDate.isNotBlank() && actualEndTime.isNotBlank())
            sdfDateTime.parse("${actualEndDate} ${actualEndTime}")
        else null
    } catch (e: Exception) { null }
    val canSave = title.isNotBlank()
            && actualStartDate.isNotBlank()
            && actualEndDate.isNotBlank()
            && (allDay || (actualStartTime.isNotBlank() && actualEndTime.isNotBlank()))
            && startDate != null
            && endDate != null
            && !endDate.before(startDate)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    showDeleteDialog = true // ダイアログを表示
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "削除", tint = Color.Red)
                }
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("タイトル") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        CategoryTabs(
            categories = categories,
            selectedCategory = category,
            onCategorySelected = { category = it }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("終日", modifier = Modifier.weight(1f))
            Switch(
                checked = allDay,
                onCheckedChange = { allDay = it }
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "開始",
                    modifier = Modifier.width(64.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                DatePickerTextButton(
                    label = "開始",
                    dateText = actualStartDate,
                    onDateSelected = { startDateText = it },
                    modifier = Modifier.weight(1f)
                )
                if (!allDay) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TimePickerTextButton(
                        label = "開始",
                        timeText = actualStartTime,
                        onTimeSelected = { startTimeText = it },
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "終了",
                    modifier = Modifier.width(64.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                DatePickerTextButton(
                    label = "終了",
                    dateText = actualEndDate,
                    onDateSelected = { endDateText = it },
                    modifier = Modifier.weight(1f)
                )
                if (!allDay) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TimePickerTextButton(
                        label = "終了",
                        timeText = actualEndTime,
                        onTimeSelected = { endTimeText = it },
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
        }

        if (startDate != null && endDate != null && endDate.before(startDate)) {
            Text(
                text = "終了日は開始日以降に設定してください",
                color = Color.Red,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        var repeatMenuExpanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("繰り返し", modifier = Modifier.weight(1f))
            Box {
                Button(onClick = { repeatMenuExpanded = true }) {
                    Text(repeatOption)
                }
                DropdownMenu(
                    expanded = repeatMenuExpanded,
                    onDismissRequest = { repeatMenuExpanded = false }
                ) {
                    listOf("しない", "毎日", "毎週", "毎月", "毎年").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                repeatOption = option
                                repeatMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("通知", modifier = Modifier.weight(1f))
            Slider(
                value = notificationMinutes.toFloat(),
                onValueChange = { notificationMinutes = it.toInt() },
                valueRange = 0f..60f,
                steps = 5,
                modifier = Modifier.weight(2f)
            )
            Text("${notificationMinutes}分前")
        }

        OutlinedTextField(
            value = memo,
            onValueChange = { memo = it },
            label = { Text("メモ") },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {
                    onEditComplete()
                    selectedDestination.value = EcoduleRoute.CALENDAR
                }
            ) {
                Text("キャンセル")
            }
            Button(
                onClick = {
                    if (!canSave) return@Button

                    if (isEditing && editingEventId != null) {
                        val event = taskViewModel.getEventById(editingEventId)
                        if (event?.repeatGroupId != null) {
                            // 繰り返しグループ全体編集
                            taskViewModel.updateEventsByRepeatGroup(
                                event.repeatGroupId,
                                title, category, description, allDay,
                                startDate, endDate, repeatOption, memo, notificationMinutes
                            )
                        } else {
                            // 単体編集
                            taskViewModel.updateEvent(
                                editingEventId,
                                title, category, description, allDay,
                                startDate, endDate, repeatOption, memo, notificationMinutes
                            )
                        }
                    } else {
                        // 追加時（1件だけ追加）
                        taskViewModel.addEvent(
                            title, category, description, allDay,
                            startDate, endDate, repeatOption, memo, notificationMinutes,
                            repeatGroupId = null
                        )
                    }

                    onEditComplete()
                    selectedDestination.value = EcoduleRoute.CALENDAR
                },
                enabled = canSave
            ) {
                Text(buttonText)
            }
        }
    }

    // 削除確認ダイアログ
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("予定を削除") },
            text = { Text("この予定を削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        editingEventId?.let { taskViewModel.deleteEvent(it) }
                        showDeleteDialog = false
                        onEditComplete()
                        selectedDestination.value = EcoduleRoute.CALENDAR
                    }
                ) {
                    Text("削除", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddTaskContentPreview() {
    val dummySelectedDestination = remember { mutableStateOf("Tasks") }
    val dummyTaskViewModel = remember { TaskViewModel() }
    AddTaskContent(
        selectedDestination = dummySelectedDestination,
        taskViewModel = dummyTaskViewModel
    )
}