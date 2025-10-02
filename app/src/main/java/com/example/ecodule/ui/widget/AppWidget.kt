package com.example.ecodule.ui.widget

import android.content.Context
import com.example.ecodule.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.color.ColorProvider // ← 関数（トップレベル）を import
import androidx.glance.currentState
import androidx.glance.unit.ColorProvider as GlanceColor // ← 型が必要な場合はこちらの別名を使用
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
//import androidx.glance.action.actionRunCallback // ← 使わない（削除候補）※混乱を避けるため残していません
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.action.actionRunCallback // ← こちらを使用
import androidx.glance.appwidget.action.ActionCallback   // ← こちらを使用
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
//import androidx.glance.state.currentState
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.format.TextStyle as JTextStyle

class AppWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Content(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ImageProvider(R.drawable.ecodule_wiget))
                        .cornerRadius(16.dp)
                        .padding(12.dp)
                )
            }
        }
    }

    @Composable
    private fun Content(modifier: GlanceModifier) {
        val prefs: Preferences = currentState()
        val tasks: List<Task> = prefs[PrefKeys.tasksJson]?.let {
            runCatching { Json.decodeFromString<List<Task>>(it) }.getOrElse { sampleTasks() }
        } ?: sampleTasks()

        val today = LocalDate.now()
        val weekDayJa = today.dayOfWeek.getDisplayName(JTextStyle.SHORT, Locale.JAPANESE)
        val dateStr = today.format(DateTimeFormatter.ofPattern("M/d"))

        val white: GlanceColor = ColorProvider(day = Color.White, night = Color.White)

        Column(modifier) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(text = "${weekDayJa}曜日", style = TextStyle(color = white, fontWeight = FontWeight.Bold))
                    Text(text = dateStr, style = TextStyle(color = white, fontWeight = FontWeight.Bold))
                }
                Button(text = "完了", onClick = actionRunCallback<NoopAction>())
            }

            Spacer(GlanceModifier.height(8.dp))

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                LazyColumn {
                    items(tasks) { task ->
                        TaskRow(task = task, textColor = white)
                    }
                }
            }
        }
    }

    @Composable
    private fun TaskRow(task: Task, textColor: GlanceColor) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable(
                    onClick = actionRunCallback<ToggleTaskAction>(
                        parameters = actionParametersOf(
                            PrefKeys.taskIdParam to task.id
                        )
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskCheck(checked = task.isDone)
            Spacer(GlanceModifier.width(10.dp))
            Text(text = task.title, style = TextStyle(color = textColor))
        }
    }

    @Composable
    private fun TaskCheck(checked: Boolean) {
        val lime: GlanceColor = ColorProvider(day = Color(0xFF8BC34A), night = Color(0xFF8BC34A))
        val transparent: GlanceColor = ColorProvider(day = Color.Transparent, night = Color.Transparent)
        val white: GlanceColor = ColorProvider(day = Color.White, night = Color.White)

        Box(
            modifier = GlanceModifier
                .size(22.dp)
                .background(if (checked) lime else transparent),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Text(text = "✓", style = TextStyle(color = white))
            }
        }
    }

    private object PrefKeys {
        val tasksJson = stringPreferencesKey("tasks_json")
        val taskIdParam = ActionParameters.Key<String>("task_id")
    }

    private fun sampleTasks(): List<Task> = listOf(
        Task(id = "1", title = "スライド作成", isDone = false),
        Task(id = "2", title = "発表練習", isDone = false),
        Task(id = "3", title = "資料印刷", isDone = true),
        Task(id = "4", title = "スライド作成", isDone = false),
        Task(id = "5", title = "発表練習", isDone = false),
        Task(id = "6", title = "資料印刷", isDone = true),
    )

    @Serializable
    data class Task(
        val id: String,
        val title: String,
        val isDone: Boolean
    )

    // ここを onAction に変更（appwidget.action.ActionCallback を実装）
    class NoopAction : ActionCallback {
        override suspend fun onAction(
            context: Context,
            glanceId: GlanceId,
            parameters: ActionParameters
        ) = Unit
    }

    class ToggleTaskAction : ActionCallback {
        override suspend fun onAction(
            context: Context,
            glanceId: GlanceId,
            parameters: ActionParameters
        ) {
            val taskId = parameters[PrefKeys.taskIdParam] ?: return

            // 1) ここは「更新後の Preferences を返す」形にする
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                val current = prefs[PrefKeys.tasksJson]?.let {
                    runCatching { Json.decodeFromString<List<Task>>(it) }.getOrNull()
                } ?: emptyList()

                val updated = current.map { if (it.id == taskId) it.copy(isDone = !it.isDone) else it }

                // 2) 読み取り専用 → 可変にして値をセットし、返す
                prefs.toMutablePreferences().apply {
                    this[PrefKeys.tasksJson] = Json.encodeToString(updated)
                }
            }

            // 3) 外側 this は使えないので、新しいインスタンスで update
            AppWidget().update(context, glanceId)
        }
    }
}