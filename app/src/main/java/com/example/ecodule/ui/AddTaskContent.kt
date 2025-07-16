package com.example.ecodule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddTaskContent(
    modifier: Modifier = Modifier,
    onSaveTask: (String, String, String, Boolean, Date?, Date?, String, String, Int) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onCancel: () -> Unit = {}
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
    val sdf = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
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
            text = "タスクを追加",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // 編集ボタン（えんぴつマーク）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { /* 編集ロジック */ }) {
                Icon(Icons.Default.Edit, contentDescription = "編集")
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

        // カテゴリータブ（追加部分）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "カテゴリー",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                categories.forEach { (cat, color) ->
                    val isSelected = category == cat
                    Text(
                        text = cat,
                        color = Color.Black,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                if (isSelected) color else Color(0xFFF4F4F4),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { category = cat }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

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
            OutlinedButton(onClick = onCancel) {
                Text("キャンセル")
            }
            Button(
                onClick = {
                    // ここで保存処理
                    val startDate = try { sdf.parse(startDateText) } catch (e: Exception) { null }
                    val endDate = try { sdf.parse(endDateText) } catch (e: Exception) { null }
                    onSaveTask(
                        title, category, description, allDay,
                        startDate, endDate, repeatOption, memo, notificationMinutes
                    )
                },
                enabled = title.isNotBlank()
            ) {
                Text("追加")
            }
        }

        // スライドバーのイメージ（画面上下スクロール）
        Box(
            Modifier
                .fillMaxWidth()
                .height(24.dp)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Divider(
                Modifier
                    .width(60.dp)
                    .height(6.dp),
                color = MaterialTheme.colorScheme.secondary,
                thickness = 6.dp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTaskContentPreview() {
    AddTaskContent()
}