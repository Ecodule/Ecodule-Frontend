package com.example.ecodule.repository.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ecodule.repository.TaskRepository
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import androidx.compose.ui.graphics.Color

val Context.taskStore: DataStore<Preferences> by preferencesDataStore("task")
fun taskKey(userId: String) = stringPreferencesKey("tasks_${userId}")

// カテゴリ名からカラーIntを返す関数
fun getCategoryColorInt(category: String): Int {
    return when (category) {
        "ゴミ出し" -> 0xFF1E688D.toInt()
        "通勤/通学" -> 0xFFAF4C30.toInt()
        "外出" -> 0xFF7CA62C.toInt()
        "買い物" -> 0xFF19A261.toInt()
        else -> 0xFFFF0000.toInt()
    }
}
fun getCategoryColor(category: String): Color {
    return when (category) {
        "ゴミ出し" -> Color(0xFF1E688D)
        "通勤/通学" -> Color(0xFFAF4C30)
        "外出" -> Color(0xFF7CA62C)
        "買い物" -> Color(0xFF19A261)
        else -> Color(0xFFFF0000)
    }
}

class DataStoreTaskRepository @Inject constructor(
    @ApplicationContext
    private val context: Context

): TaskRepository {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun observeTasks(userId: String): StateFlow<List<CalendarEvent>> =
        context.taskStore.data
            .map { pref ->
                Log.d("fromDataStore", "$pref")
                val tasksJson = pref[taskKey(userId)] ?: "[]"
                try {
                    val events = Json.decodeFromString(ListSerializer(CalendarEvent.serializer()), tasksJson)
                    // category/colorIntからcolorプロパティを復元
                    events.map { event ->
                        val expectedColorInt = getCategoryColorInt(event.category)
                        val colorIntToUse = if (event.colorInt == 0xFFB3E6FF.toInt() && event.category.isNotBlank()) {
                            // 初期値ならカテゴリから色を決定
                            expectedColorInt
                        } else {
                            event.colorInt
                        }
                        event.copy(
                            color = Color(colorIntToUse)
                        )
                    }
                } catch (e: Exception) {
                    Log.e("DataStoreTaskRepository", "Failed to deserialize tasks: ${e.message}")
                    emptyList()
                }
            }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun addTask(userId: String, newTask: CalendarEvent) {
        context.taskStore.edit { pref ->
            val tasksJson = pref[taskKey(userId)] ?: "[]"
            val currentTasks = try {
                Json.decodeFromString(ListSerializer(CalendarEvent.serializer()), tasksJson)
            } catch (e: Exception) {
                Log.e("DataStoreTaskRepository", "Failed to deserialize tasks: ${e.message}")
                emptyList()
            }
            // categoryに応じてcolorIntとcolorを正しく設定する
            val categoryColorInt = getCategoryColorInt(newTask.category)
            val eventToSave = newTask.copy(
                colorInt = categoryColorInt,
                color = Color(categoryColorInt)
            )
            val updatedTasks = currentTasks + eventToSave
            pref[taskKey(userId)] = Json.encodeToString(ListSerializer(CalendarEvent.serializer()), updatedTasks)
        }
    }

    override suspend fun deleteTask(userId: String, eventId: String) {
        context.taskStore.edit { pref ->
            val tasksJson = pref[taskKey(userId)] ?: "[]"
            val currentTasks = try {
                Json.decodeFromString(ListSerializer(CalendarEvent.serializer()), tasksJson)
            } catch (e: Exception) {
                Log.e("DataStoreTaskRepository", "Failed to deserialize tasks: ${e.message}")
                emptyList()
            }
            val updatedTasks = currentTasks.filterNot { it.id == eventId }
            pref[taskKey(userId)] = Json.encodeToString(ListSerializer(CalendarEvent.serializer()), updatedTasks)
        }
    }

    override suspend fun clearTasks(userId: String) {
        context.taskStore.edit { pref ->
            pref.remove(taskKey(userId))
        }
    }
}