package com.example.ecodule.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SettingsContentScreen(
    modifier: Modifier = Modifier,
    onNavigateUserName: () -> Unit = {},
    onNavigateTimeZone: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateGoogleCalendar: () -> Unit = {},
    onNavigateDetail: () -> Unit = {}
) {
    var useDeviceTimeZone by remember { mutableStateOf(true) }
    var showWeekNumbers by remember { mutableStateOf(true) }
    var selectedWeekStart by remember { mutableStateOf("日曜日") }
    var expandedWeekStart by remember { mutableStateOf(false) }
    val weekStartOptions = listOf("土曜日", "日曜日", "月曜日")

    var selectedTaskDuration by remember { mutableStateOf("60 分") }
    var expandedTaskDuration by remember { mutableStateOf(false) }
    val taskDurationOptions = listOf("15 分", "30 分", "45 分", "60 分", "90 分", "120 分")

    var selectedTimeZone by remember { mutableStateOf("日本標準時") }

    val horizontalItemPadding = PaddingValues(start = 20.dp, end = 16.dp)
    val rightEndIconPadding = 8.dp
    val switchRightPadding = 8.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(top = 32.dp)
    ) {
        // タイトル
        Text(
            text = "設定",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(18.dp))

        // ユーザー名
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .clickable { onNavigateUserName() }
                .padding(vertical = 12.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "ユーザーアイコン",
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "ユーザー名",
                fontSize = 20.sp,
                color = Color(0xFF444444),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "ユーザー名詳細へ",
                tint = Color(0xFF888888),
                modifier = Modifier
                    .padding(end = rightEndIconPadding)
                    .size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // カレンダーセクション
        Text(
            text = "カレンダー",
            fontSize = 14.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(start = 32.dp, bottom = 4.dp)
        )

        // 週の開始日
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(vertical = 12.dp)
                .padding(horizontal = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "週の開始日",
                    fontSize = 18.sp,
                    color = Color(0xFF444444),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                )
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { expandedWeekStart = true }
                            .padding(end = 2.dp)
                    ) {
                        Text(
                            text = selectedWeekStart,
                            fontSize = 16.sp,
                            color = Color(0xFF888888)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "週の開始日選択",
                            tint = Color(0xFF888888),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expandedWeekStart,
                        onDismissRequest = { expandedWeekStart = false },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        weekStartOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (option == selectedWeekStart) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Text(option)
                                    }
                                },
                                onClick = {
                                    selectedWeekStart = option
                                    expandedWeekStart = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // デバイスタイムゾーンとタイムゾーンを1枠にし、線で区切る
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "デバイスのタイムゾーンを使用",
                    fontSize = 18.sp,
                    color = Color(0xFF444444),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                )
                Switch(
                    checked = useDeviceTimeZone,
                    onCheckedChange = { useDeviceTimeZone = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFFFFFF),
                        checkedTrackColor = Color(0xFF8CC447)
                    ),
                    modifier = Modifier.padding(end = switchRightPadding)
                )
            }
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !useDeviceTimeZone) { onNavigateTimeZone() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "タイムゾーン",
                    fontSize = 18.sp,
                    color = if (useDeviceTimeZone) Color(0xFFBBBBBB) else Color(0xFF444444),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                )
                Text(
                    text = selectedTimeZone,
                    fontSize = 16.sp,
                    color = if (useDeviceTimeZone) Color(0xFFBBBBBB) else Color(0xFF888888)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "タイムゾーン詳細へ",
                    tint = if (useDeviceTimeZone) Color(0xFFBBBBBB) else Color(0xFF888888),
                    modifier = Modifier
                        .padding(end = rightEndIconPadding)
                        .size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 週数を表示する
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(vertical = 12.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "週数を表示する",
                fontSize = 18.sp,
                color = Color(0xFF444444),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            )
            Switch(
                checked = showWeekNumbers,
                onCheckedChange = { showWeekNumbers = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFFFFFFF),
                    checkedTrackColor = Color(0xFF8CC447)
                ),
                modifier = Modifier.padding(end = switchRightPadding)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 既定のタスクの長さ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(vertical = 12.dp)
                .padding(horizontal = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "既定のタスクの長さ",
                    fontSize = 18.sp,
                    color = Color(0xFF444444),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                )
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { expandedTaskDuration = true }
                            .padding(end = 2.dp)
                    ) {
                        Text(
                            text = selectedTaskDuration,
                            fontSize = 16.sp,
                            color = Color(0xFF888888)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "既定のタスクの長さ選択",
                            tint = Color(0xFF888888),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expandedTaskDuration,
                        onDismissRequest = { expandedTaskDuration = false },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        taskDurationOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (option == selectedTaskDuration) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Text(option)
                                    }
                                },
                                onClick = {
                                    selectedTaskDuration = option
                                    expandedTaskDuration = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // アプリセクション
        Text(
            text = "アプリ",
            fontSize = 14.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(start = 32.dp, bottom = 4.dp)
        )

        // 通知
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .clickable { onNavigateNotifications() }
                .padding(vertical = 12.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "通知",
                fontSize = 20.sp,
                color = Color(0xFF444444),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "通知詳細へ",
                tint = Color(0xFF888888),
                modifier = Modifier
                    .padding(end = rightEndIconPadding)
                    .size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Googleカレンダーと連携
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .clickable { onNavigateGoogleCalendar() }
                .padding(vertical = 12.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Google カレンダーと連携する",
                fontSize = 18.sp,
                color = Color(0xFF444444),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Googleカレンダー詳細へ",
                tint = Color(0xFF888888),
                modifier = Modifier
                    .padding(end = rightEndIconPadding)
                    .size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 詳細
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .clickable { onNavigateDetail() }
                .padding(vertical = 12.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "詳細",
                fontSize = 18.sp,
                color = Color(0xFF444444),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "詳細へ",
                tint = Color(0xFF888888),
                modifier = Modifier
                    .padding(end = rightEndIconPadding)
                    .size(24.dp)
            )
        }
    }
}