package com.example.ecodule.ui.taskListContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import kotlin.math.roundToInt

// カテゴリごとの色定義
val CategoryColorMap = mapOf(
    "ゴミ出し" to Color(0xFFB3E6FF),
    "通勤/通学" to Color(0xFFFFD2C5),
    "外出" to Color(0xFFE4EFCF),
    "買い物" to Color(0xFFC9E4D7)
)

/**
 * 背景色を暗く変換するヘルパー関数
 */
fun darkenColor(color: Color, factor: Float = 0.6f): Color {
    // factor: 0.0 = 真っ黒, 1.0 = 変化なし
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

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(todayEvents.size) { index ->
                val event = todayEvents[index]
                val ecoActions = getEcoActionsForLabel(event.category)
                val bgColor = CategoryColorMap[event.category] ?: Color(0xFFE0E0E0) // デフォルトグレー

                TaskSectionWithTitleAndTime(
                    event = event,
                    items = ecoActions,
                    checkedStates = checkedStates,
                    backgroundColor = bgColor, // 色を渡す
                    onCheckedChange = { label, checked ->
                        val key = "${event.label}-${label}-${event.startDate.hour}"
                        viewModel.setChecked(key, checked)
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
    backgroundColor: Color, // 追加
    onCheckedChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Cardで角丸背景
    Card(
        modifier = modifier.padding(horizontal = 2.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
            Spacer(Modifier.height(0.dp)) // 少し余白

            val checkboxColor = darkenColor(backgroundColor, 0.6f)
            Column(verticalArrangement = Arrangement.spacedBy(-(20).dp)) {
                items.forEach { label ->
                    val key = "${event.label}-${label}-${event.startDate.hour}"
                    val checked = checkedStates[key] ?: false
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { onCheckedChange(label, it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = checkboxColor,
                                uncheckedColor = checkboxColor,
                                checkmarkColor = Color.White
                            )
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
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