package com.example.ecodule.ui.taskListContent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val CategoryColorMap = mapOf(
    "ゴミ出し" to Color(0xFFB3E6FF),
    "通勤/通学" to Color(0xFFFFD2C5),
    "外出" to Color(0xFFE4EFCF),
    "買い物" to Color(0xFFC9E4D7)
)

fun darkenColor(color: Color, factor: Float = 0.6f): Color {
    val r = (color.red * factor).coerceIn(0f, 1f)
    val g = (color.green * factor).coerceIn(0f, 1f)
    val b = (color.blue * factor).coerceIn(0f, 1f)
    return Color(r, g, b, color.alpha)
}

@Composable
fun TaskListContent(
    todayEvents: List<CalendarEvent>,
    modifier: Modifier = Modifier,
    viewModel: TaskCheckViewModel = viewModel()
) {
    if (todayEvents.isNotEmpty()) {
        val checkedStates by viewModel.checkedStates.collectAsState()
        val expandedStates by viewModel.expandedStates.collectAsState()

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(todayEvents.size) { index ->
                val event = todayEvents[index]
                val ecoActions = getEcoActionsForLabel(event.category)
                val bgColor = CategoryColorMap[event.category] ?: Color(0xFFE0E0E0)

                val eventKey = "${event.label}-${event.startDate.hour}"

                TaskSectionWithTitleAndTime(
                    event = event,
                    items = ecoActions,
                    checkedStates = checkedStates,
                    expanded = expandedStates[eventKey] ?: false,
                    backgroundColor = bgColor,
                    onCheckedChange = { label, checked ->
                        val key = "${event.label}-${label}-${event.startDate.hour}"
                        viewModel.setChecked(key, checked)
                    },
                    onExpandToggle = {
                        viewModel.toggleExpanded(eventKey)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "今日のタスクはありません",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

class TaskCheckViewModel : ViewModel() {
    private val _checkedStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedStates: StateFlow<Map<String, Boolean>> = _checkedStates

    private val _expandedStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedStates: StateFlow<Map<String, Boolean>> = _expandedStates

    fun setChecked(key: String, checked: Boolean) {
        _checkedStates.value = _checkedStates.value.toMutableMap().apply {
            put(key, checked)
        }
    }

    fun toggleExpanded(key: String) {
        _expandedStates.value = _expandedStates.value.toMutableMap().apply {
            val current = this[key] ?: false
            put(key, !current)
        }
    }
}

// 行動データクラス
data class EcoAction(
    val label: String,
    val co2Kg: Double, // CO2削減量
    val savedYen: Int  // 節約額
)

@Composable
private fun TaskSectionWithTitleAndTime(
    event: CalendarEvent,
    items: List<EcoAction>,
    checkedStates: Map<String, Boolean>,
    expanded: Boolean,
    backgroundColor: Color,
    onCheckedChange: (String, Boolean) -> Unit,
    onExpandToggle: () -> Unit,
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
            if (event.startDate.hour != null && event.endDate.hour != null) {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { onCheckedChange(ecoAction.label, it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = checkboxColor,
                                    uncheckedColor = checkboxColor,
                                    checkmarkColor = Color.White
                                )
                            )
                            Column {
                                Text(
                                    text = ecoAction.label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "CO₂削減量: ${ecoAction.co2Kg}kg / 節約額: ¥${ecoAction.savedYen}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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

// 各カテゴリの行動データ（CO2・節約額は仮値）
fun getEcoActionsForLabel(category: String): List<EcoAction> {
    val normalized = category.trim()
    return when (normalized) {
        "買い物" -> listOf(
            EcoAction("マイバッグを持参する", co2Kg = 0.02, savedYen = 5),
            EcoAction("買い物リストを事前に作る", co2Kg = 0.01, savedYen = 10),
            EcoAction("地元の野菜・商品を選ぶ", co2Kg = 0.05, savedYen = 15),
            EcoAction("レジ袋を断る", co2Kg = 0.02, savedYen = 5)
        )
        "外出" -> listOf(
            EcoAction("徒歩や自転車で移動する", co2Kg = 0.1, savedYen = 50),
            EcoAction("公共交通機関を利用する", co2Kg = 0.05, savedYen = 30),
            EcoAction("エコボトルを持参する", co2Kg = 0.01, savedYen = 10),
            EcoAction("ゴミは持ち帰る", co2Kg = 0.01, savedYen = 0)
        )
        "ゴミ出し" -> listOf(
            EcoAction("ゴミを分別する", co2Kg = 0.03, savedYen = 0),
            EcoAction("生ゴミはコンポスト利用", co2Kg = 0.05, savedYen = 0),
            EcoAction("ゴミ袋を再利用する", co2Kg = 0.02, savedYen = 5),
            EcoAction("ゴミ出しは決められた時間に行う", co2Kg = 0.01, savedYen = 0)
        )
        "通勤/通学" -> listOf(
            EcoAction("徒歩や自転車で通う", co2Kg = 0.2, savedYen = 100),
            EcoAction("エコバッグ・水筒を持参する", co2Kg = 0.01, savedYen = 10),
            EcoAction("公共交通機関を使う", co2Kg = 0.07, savedYen = 50),
            EcoAction("職場・学校で省エネを意識する", co2Kg = 0.03, savedYen = 30)
        )
        else -> listOf(
            EcoAction("未設定の環境行動です", co2Kg = 0.0, savedYen = 0),
            EcoAction("ラベルを確認してください", co2Kg = 0.0, savedYen = 0),
        )
    }
}