package com.example.ecodule.ui.taskListContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TaskListContent(
    todayEvents: List<CalendarEvent>,
    modifier: Modifier = Modifier,
    viewModel: TaskCheckViewModel = viewModel()
) {
    if (todayEvents.isNotEmpty()) {
        val checkedStates by viewModel.checkedStates.collectAsState()

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(todayEvents.size) { index ->
                val event = todayEvents[index]
                val ecoActions = getEcoActionsForLabel(event.category)
                TaskSectionWithTitleAndTime(
                    event = event,
                    items = ecoActions,
                    checkedStates = checkedStates,
                    onCheckedChange = { label, checked ->
                        val key = "${event.label}-${label}-${event.startDate.hour}"
                        viewModel.setChecked(key, checked)
                    }
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
                text = "今日のタスクはありません", // 必要ならstringResourceに変更
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

    fun setChecked(key: String, checked: Boolean) {
        _checkedStates.value = _checkedStates.value.toMutableMap().apply {
            put(key, checked)
        }
    }
}

@Composable
private fun TaskSectionWithTitleAndTime(
    event: CalendarEvent,
    items: List<String>,
    checkedStates: Map<String, Boolean>,
    onCheckedChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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

        Column(verticalArrangement = Arrangement.spacedBy(-(15).dp)) {
            items.forEach { label ->
                val key = "${event.label}-${label}-${event.startDate.hour}"
                val checked = checkedStates[key] ?: false
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = checked, onCheckedChange = { onCheckedChange(label, it) })
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

fun getEcoActionsForLabel(category: String): List<String> {
    val normalized = category.trim()
    return when (normalized) {
        "買い物" -> listOf(
            "マイバッグを持参する",
            "買い物リストを事前に作る",
            "地元の野菜・商品を選ぶ",
            "レジ袋を断る"
        )
        "外出" -> listOf(
            "徒歩や自転車で移動する",
            "公共交通機関を利用する",
            "エコボトルを持参する",
            "ゴミは持ち帰る"
        )
        "ゴミ出し" -> listOf(
            "ゴミを分別する",
            "生ゴミはコンポスト利用",
            "ゴミ袋を再利用する",
            "ゴミ出しは決められた時間に行う"
        )
        "通勤/通学" -> listOf(
            "徒歩や自転車で通う",
            "エコバッグ・水筒を持参する",
            "公共交通機関を使う",
            "職場・学校で省エネを意識する"
        )
        else -> listOf(
            "未設定の環境行動です",
            "ラベルを確認してください",
        )
    }
}
