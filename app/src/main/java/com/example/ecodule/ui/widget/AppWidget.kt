package com.example.ecodule.ui.widget

import android.content.Context
import com.example.ecodule.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.unit.ColorProvider as GlanceColor // ← 型が必要な場合はこちらの別名を使用
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.action.ToggleableStateKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.ActionCallback
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
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
//import androidx.glance.state.currentState
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.serialization.Serializable
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

        val black: GlanceColor = ColorProvider(day = Color.Black, night = Color.Black)

        Column(modifier) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier) {
                    Text(
                        text = "${weekDayJa}曜日",
                        style = TextStyle(color = black, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = dateStr,
                        // 本日の日付のフォントサイズ指定箇所（拡大）
                        style = TextStyle(
                            color = black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                }
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(text="日曜日のゴミ出し", style = TextStyle(color = black, fontWeight = FontWeight.Bold) )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Button(
                    text = "完了",
                    onClick = actionRunCallback<CompleteAllAction>()
                )
            }

            Spacer(GlanceModifier.height(4.dp))

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(140.dp) // 4~5件程度見える高さ
            ) {
                LazyColumn {
                    items(tasks) { task ->
                        TaskRow(task = task, textColor = black)
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
                // 左右に十分な余白を付けて、CheckBox のにじみ・欠けを防止
                .padding(horizontal = 3.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // size 指定は削除し、デフォルトサイズに任せる
            // 右側に十分な余白を付けて Text と干渉しないようにする
            CheckBox(
                checked = task.isDone,
                onCheckedChange = actionRunCallback<SetTaskCheckedAction>(
                    parameters = actionParametersOf(
                        PrefKeys.taskIdParam to task.id
                        // 新しい状態は ToggleableStateKey に自動で入る
                    )
                ),
                modifier = GlanceModifier.padding(end = 8.dp)
            )

            // 残り幅をすべて使ってテキストを配置（干渉回避）
            Text(
                text = task.title,
                style = TextStyle(color = textColor, fontSize = 16.sp),
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }

    private object PrefKeys {
        val tasksJson = stringPreferencesKey("tasks_json")
        val taskIdParam = ActionParameters.Key<String>("task_id")
    }

    private fun sampleTasks(): List<Task> = listOf(
        Task(id = "1", title = "ゴミを分別する", isDone = false),
        Task(id = "2", title = "生ごみはコンポスト利用", isDone = false),
        Task(id = "3", title = "ゴミ袋を再利用する", isDone = true),
        Task(id = "4", title = "ゴミ出しは決められた時間に行う", isDone = false),
    )

    @Serializable
    data class Task(
        val id: String,
        val title: String,
        val isDone: Boolean
    )

    // 「すべて完了」：全て true にし、必要ならサーバーへバルク送信
    class CompleteAllAction : ActionCallback {
        override suspend fun onAction(
            context: Context,
            glanceId: GlanceId,
            parameters: ActionParameters
        ) {
            var idsToMark: List<String> = emptyList()

            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                val current = prefs[PrefKeys.tasksJson]?.let {
                    runCatching { Json.decodeFromString<List<Task>>(it) }.getOrNull()
                } ?: emptyList()

                idsToMark = current.filter { !it.isDone }.map { it.id }
                val updated = current.map { it.copy(isDone = true) }

                prefs.toMutablePreferences().apply {
                    this[PrefKeys.tasksJson] = Json.encodeToString(updated)
                }
            }

            if (idsToMark.isNotEmpty()) {
                runCatching { TaskRepository.markTasksDone(idsToMark) }
            }

            AppWidget().update(context, glanceId)
        }
    }

    // CheckBox の onCheckedChange から呼ばれる
    class SetTaskCheckedAction : ActionCallback {
        override suspend fun onAction(
            context: Context,
            glanceId: GlanceId,
            parameters: ActionParameters
        ) {
            val taskId = parameters[PrefKeys.taskIdParam] ?: return
            val newChecked = parameters[ToggleableStateKey] ?: return

            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                val current = prefs[PrefKeys.tasksJson]?.let {
                    runCatching { Json.decodeFromString<List<Task>>(it) }.getOrNull()
                } ?: emptyList()

                val updated = current.map { if (it.id == taskId) it.copy(isDone = newChecked) else it }

                prefs.toMutablePreferences().apply {
                    this[PrefKeys.tasksJson] = Json.encodeToString(updated)
                }
            }

            if (newChecked) {
                runCatching { TaskRepository.markTaskDone(taskId) }
            }

            AppWidget().update(context, glanceId)
        }
    }
}