package com.example.ecodule.ui.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

// 初期化（今日の一つ目の予定、EcoAction、チェック状態を保存）
suspend fun ensureInitialized(context: Context, glanceId: GlanceId) {
    val ep = EntryPointAccessors.fromApplication(context, AppWidgetEntryPoint::class.java)
    val user = ep.userRepository().user.first()
    val userId = user?.id ?: ""

    Log.d("AppWidget", "ensureInitialized: userId=$user")

    if (userId.isEmpty()) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                removeAllForWidget()
            }
        }
        return
    }

//    val todayEvents = try {
//        ep.taskRepository().observeTasks(userId).first()
//            .filter { it.startDate.toLocalDate() == LocalDate.now() }
//            .sortedBy { it.startDate }
//    } catch (e: Exception) {
//        Log.e("AppWidget", "Failed to load tasks: ${e.message}")
//        emptyList()
//    }
    val todayEvents = try {
        val today = LocalDate.now()
        Log.d("AppWidget", "Loading tasks for userId: $userId, today: $today")

        val allTasks = ep.taskRepository().observeTasks(userId).first()
        Log.d("AppWidget", "Total tasks loaded: ${allTasks.size}")

        val filtered = allTasks.filter { task ->
            val taskDate = task.startDate.toLocalDate()
            Log.d("AppWidget", "Task: ${task.label}, date: $taskDate, matches today: ${taskDate == today}")
            taskDate == today
        }.sortedBy { it.startDate }

        Log.d("AppWidget", "Today's events count: ${filtered.size}")
        filtered
    } catch (e: Exception) {
        Log.e("AppWidget", "Failed to load tasks", e)
        emptyList()
    }

    val first = todayEvents.firstOrNull()
    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
        val mutablePrefs = prefs.toMutablePreferences()
        if (first == null) {
            mutablePrefs.removeAllForWidget()
        } else {
            val eventState = CurrentEventState.from(first)
            mutablePrefs[PrefKeys.currentEventJson] =
                Json.encodeToString(CurrentEventState.serializer(), eventState)
            mutablePrefs[PrefKeys.currentEventId] = eventState.id

            val ecoList = loadEcoActionsForEvent(ep, CurrentEventState.from(first))
            mutablePrefs[PrefKeys.ecoActionsJson] =
                Json.encodeToString(ListSerializer(EcoActionItem.serializer()), ecoList)

            val checkedMapForUser = runCatching {
                ep.checkedStateRepository().observeCheckedStates(userId).first()
            }.getOrElse { emptyMap() }

            val entries = ecoList.map { item ->
                val key = buildCheckedKey(eventState, item)
                CheckedEntry(key, checked = checkedMapForUser[key] ?: false)
            }
            mutablePrefs[PrefKeys.checkedJson] =
                Json.encodeToString(ListSerializer(CheckedEntry.serializer()), entries)
        }
        mutablePrefs
    }
}

suspend fun loadEcoActionsForEvent(
    ep: AppWidgetEntryPoint,
    event: CurrentEventState
): List<EcoActionItem> {
    val catEnum = mapCategoryToEnum(event.category) ?: return emptyList()
    return runCatching {
        ep.ecoActionRepository().getActionsForCategory(catEnum).mapToItem()
    }.getOrElse { emptyList() }
}