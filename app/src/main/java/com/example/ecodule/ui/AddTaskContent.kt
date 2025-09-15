package com.example.ecodule.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.ecodule.ui.components.CategoryTabs
import com.example.ecodule.ui.CalendarContent.model.TaskViewModel
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Date
import java.util.Locale

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
    // State variables
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ゴミ出し") }
    var description by remember { mutableStateOf("") }
    var allDay by remember { mutableStateOf(false) }
    var startDateText by remember { mutableStateOf("") }
    var endDateText by remember { mutableStateOf("") }
    var repeatOption by remember { mutableStateOf("しない") }
    var notificationMinutes by remember { mutableStateOf(10) }
    var memo by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) } // 削除確認ダイアログの状態
    val sdf = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    val isEditing = editingEventId != null
    val titleText = if (isEditing) "タスクを編集" else "タスクを追加"
    val buttonText = if (isEditing) "更新" else "追加"

    // 編集時のデータ読み込み
    LaunchedEffect(editingEventId) {
        if (editingEventId != null) {
            val event = taskViewModel.getEventById(editingEventId)
            event?.let {
                title = it.label
                category = it.category
                allDay = it.allDay
                startDateText = it.startDate?.let { date ->
                    sdf.format(Date.from(date.atZone(ZoneId.systemDefault()).toInstant()))
                } ?: ""
                endDateText = it.endDate?.let { date ->
                    sdf.format(Date.from(date.atZone(ZoneId.systemDefault()).toInstant()))
                } ?: ""
                repeatOption = it.repeatOption
                memo = it.memo
                notificationMinutes = it.notificationMinutes
            }
        } else {
            // 新規作成時は初期化
            title = ""
            category = "ゴミ出し"
            description = ""
            allDay = false
            startDateText = ""
            endDateText = ""
            repeatOption = "しない"
            notificationMinutes = 10
            memo = ""
        }
    }

    // カテゴリータブ情報
    val categories = listOf(
        "ゴミ出し" to Color(0xFFB3E6FF),
        "通勤/通学" to Color(0xFFFFD2C5),
        "外出" to Color(0xFFE4EFCF),
        "買い物" to Color(0xFFC9E4D7)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // タイトル
        Text(
            text = titleText,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // 編集・削除ボタン（編集時のみ表示）
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

        // タスクタイトル入力
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

        // カテゴリータブ
        CategoryTabs(
            categories = categories,
            selectedCategory = category,
            onCategorySelected = { category = it }
        )

        // 終日スイッチ
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

        // 開始日
        OutlinedTextField(
            value = startDateText,
            onValueChange = { startDateText = it },
            label = { Text("開始日 (yyyy/MM/dd HH:mm)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            singleLine = true,
        )

        // 終了日
        OutlinedTextField(
            value = endDateText,
            onValueChange = { endDateText = it },
            label = { Text("終了日 (yyyy/MM/dd HH:mm)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            singleLine = true,
        )

        // 繰り返し設定
        var repeatMenuExpanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("繰り返し", modifier = Modifier.weight(1f))
            Button(onClick = { repeatMenuExpanded = true }) {
                Text(repeatOption)
            }
            DropdownMenu(
                expanded = repeatMenuExpanded,
                onDismissRequest = { repeatMenuExpanded = false }
            ) {
                listOf("しない", "毎日", "毎週", "毎月", "カスタム").forEach { option ->
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

        // 通知設定
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

        // メモ欄
        OutlinedTextField(
            value = memo,
            onValueChange = { memo = it },
            label = { Text("メモ") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 8.dp)
        )

        // 保存・キャンセルボタン
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
                    val startDate = try { sdf.parse(startDateText) } catch (e: Exception) { null }
                    val endDate = try { sdf.parse(endDateText) } catch (e: Exception) { null }

                    if (isEditing && editingEventId != null) {
                        taskViewModel.updateEvent(
                            editingEventId,
                            title, category, description, allDay,
                            startDate, endDate, repeatOption, memo, notificationMinutes
                        )
                    } else {
                        taskViewModel.addEvent(
                            title, category, description, allDay,
                            startDate, endDate, repeatOption, memo, notificationMinutes
                        )
                    }

                    onEditComplete()
                    selectedDestination.value = EcoduleRoute.CALENDAR
                },
                enabled = title.isNotBlank()
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