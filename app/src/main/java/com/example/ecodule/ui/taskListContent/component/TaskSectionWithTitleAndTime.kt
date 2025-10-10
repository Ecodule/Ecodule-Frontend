package com.example.ecodule.ui.taskListContent.component

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.ecodule.repository.EcoAction
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.taskListContent.darkenColor
import com.example.ecodule.ui.taskListContent.api.UpdateAchievement
import com.example.ecodule.ui.taskListContent.model.TaskListViewModel
import com.example.ecodule.ui.taskListContent.TaskListInformation
import com.example.ecodule.ui.taskListContent.InfoIconButton
import com.example.ecodule.ui.taskListContent.TaskInfoDialog
import kotlin.collections.forEach


@Composable
fun TaskSectionWithTitleAndTime(
    event: CalendarEvent,
    items: List<EcoAction>,
    checkedStates: Map<String, Boolean>,
    expanded: Boolean,
    backgroundColor: Color,
    onExpandToggle: () -> Unit,
    taskListViewModel: TaskListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clickable { onExpandToggle() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = event.label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (event.startDate.hour != 0 && event.endDate.hour != 0) {
                Text(
                    text = "${event.startDate.hour}:00 ～ ${event.endDate.hour}:00",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(0.dp))

            // タップで展開
            if (expanded) {
                val checkboxColor = darkenColor(backgroundColor, 0.6f)
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    items.forEach { ecoAction ->
                        val key = "${event.label}-${ecoAction.label}-${event.startDate.hour}"
                        val checked = checkedStates[key] ?: false

                        val isLoading by taskListViewModel.isSendingAchievement.collectAsState()
                        val errorMessage by taskListViewModel.sendingAchievementError.collectAsState()

                        // 各タスク行（右端に「ⓘ」を配置し、タップで説明ポップアップ）
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // チェックボックスとローディングインジケーターの切り替え
                            if (isLoading[key] == true) {
                                CircularProgressIndicator(
                                    color = Color.Gray,
                                    strokeWidth = 3.dp,
                                )
                            } else {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = {
                                        taskListViewModel.setChecked(key, it, ecoAction)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = checkboxColor,
                                        uncheckedColor = checkboxColor,
                                        checkmarkColor = Color.White
                                    )
                                )
                            }

                            // タスク名と数値情報（可読性のため余白を少し追加、右端まで広げる）
                            Column(modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                            ) {
                                Text(
                                    text = ecoAction.label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "節約: ¥${ecoAction.savedYen} / CO₂: ${ecoAction.co2Kg}kg  ",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                if (errorMessage[key] != null) {
                                    Text(
                                        text = "エラー: ${errorMessage[key]}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            // 右端のインフォメーションボタン
                            var showInfo by remember(key) { mutableStateOf(false) }
                            InfoIconButton(
                                onClick = { showInfo = true },
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(32.dp)
                            )

                            // ポップアップ（説明は単一ソース TaskListInformation から取得）
                            if (showInfo) {
                                TaskInfoDialog(
                                    title = ecoAction.label,
                                    description = TaskListInformation.descriptionFor(
                                        taskId = null, // 安定IDがあればここに渡してください
                                        title = ecoAction.label
                                    ),
                                    onDismiss = { showInfo = false }
                                )
                            }
                        }
                    }
                }
            } else {
                // 展開前のヒント
                Text(
                    text = "タップして詳細を表示",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}