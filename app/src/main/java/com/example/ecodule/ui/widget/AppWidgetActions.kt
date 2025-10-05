package com.example.ecodule.ui.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.ToggleableStateKey
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.taskListContent.api.UpdateAchievement
import com.example.ecodule.ui.taskListContent.api.UpdateAchievementResult
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class NextEventAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: androidx.glance.action.ActionParameters) {
        val ep = EntryPointAccessors.fromApplication(context, AppWidgetEntryPoint::class.java)
        val user = ep.userRepository().user.first()
        val userId = user?.id ?: ""

        // 現在イベントIDを取得
        var currentEventId: String? = null
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            currentEventId = prefs[PrefKeys.currentEventId]
            prefs
        }

        Log.d("AppWidget", "NextEventAction: currentEventId=$currentEventId")

        val todayEvents = try {
            val today = LocalDate.now()
            // ★★★ ここを修正 ★★★
            // observeTasks(...).first() の代わりに getTasksOnce(...) を呼び出す
            val allTasks = ep.taskRepository().getTasksOnce(userId)
            Log.d("AppWidget", "Total tasks loaded with getTasksOnce: ${allTasks.size}")

            allTasks.filter { it.startDate.toLocalDate() == today }
                .sortedBy { it.startDate }
        } catch (e: Exception) {
            Log.e("AppWidget", "Failed to load tasks", e)
            emptyList()
        }

        if (todayEvents.isEmpty()) {
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    removeAllForWidget()
                }
            }
            AppWidget().update(context, glanceId)
            return
        }

        val idx = todayEvents.indexOfFirst { it.id == currentEventId }
        val next: CalendarEvent? = when {
            idx == -1 -> todayEvents.first()
            idx + 1 < todayEvents.size -> todayEvents[idx + 1]
            else -> null
        }

        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                if (next == null) {
                    removeAllForWidget()
                    return@apply
                }
                val eventState = next
                this[PrefKeys.currentEventJson] =
                    Json.encodeToString(CurrentEventState.serializer(), CurrentEventState.from(eventState))
                this[PrefKeys.currentEventId] = eventState.id

                val ecoList = loadEcoActionsForEvent(ep, CurrentEventState.from(next))
                this[PrefKeys.ecoActionsJson] =
                    Json.encodeToString(ListSerializer(EcoActionItem.serializer()), ecoList)

                val checkedMapForUser = runCatching {
                    ep.checkedStateRepository().getCheckedStatesOnce(userId)
                }.getOrElse { emptyMap() }

                Log.d("AppWidget", "Checked states loaded for user $userId: ${checkedMapForUser.size} items")

                val entries = ecoList.map { item ->
                    val key = buildCheckedKey(CurrentEventState.from(next), item)
                    CheckedEntry(key, checked = checkedMapForUser[key] ?: false)
                }
                this[PrefKeys.checkedJson] =
                    Json.encodeToString(ListSerializer(CheckedEntry.serializer()), entries)
            }
        }

        AppWidget().update(context, glanceId)
    }
}

class ToggleEcoActionAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: androidx.glance.action.ActionParameters) {
        val ep = EntryPointAccessors.fromApplication(context, AppWidgetEntryPoint::class.java)
        val user = ep.userRepository().user.first()
        val userId = user?.id ?: ""
        val userEmail = user?.email ?: ""
        val token = ep.tokenManager().getAccessToken(userEmail) ?: ""

        val ecoActionId = parameters[PrefKeys.ecoActionIdParam] ?: return
        val newChecked = parameters[ToggleableStateKey] ?: return

        // 現在の状態を読み取り
        var eventState: CurrentEventState? = null
        var ecoList: List<EcoActionItem> = emptyList()
        var checkedEntries: List<CheckedEntry> = emptyList()

        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            eventState = prefs[PrefKeys.currentEventJson]?.let {
                runCatching { Json.decodeFromString(CurrentEventState.serializer(), it) }.getOrNull()
            }
            ecoList = prefs[PrefKeys.ecoActionsJson]?.let {
                runCatching { Json.decodeFromString(ListSerializer(EcoActionItem.serializer()), it) }.getOrElse { emptyList() }
            } ?: emptyList()
            checkedEntries = prefs[PrefKeys.checkedJson]?.let {
                runCatching { Json.decodeFromString(ListSerializer(CheckedEntry.serializer()), it) }.getOrElse { emptyList() }
            } ?: emptyList()
            prefs
        }

        val ev = eventState ?: return
        val eco = ecoList.firstOrNull { it.id == ecoActionId } ?: return
        val key = buildCheckedKey(ev, eco)

        // DataStore（永続化）
        runCatching {
            ep.checkedStateRepository().setChecked(userId, key, newChecked)
        }.onFailure { e -> Log.e("AppWidget", "setChecked failed: ${e.message}") }

        // API 更新（サイレント）
        if (token.isNotBlank() && userId.isNotBlank()) {
            val co2Delta = if (newChecked) eco.co2Kg else -eco.co2Kg
            val moneyDelta = if (newChecked) eco.savedYen else -eco.savedYen
            runCatching {
                UpdateAchievement.updateAchievement(
                    accessToken = token,
                    userId = userId,
                    co2 = co2Delta,
                    money = moneyDelta
                )
            }.onFailure { e ->
                Log.e("AppWidget", "UpdateAchievement failed: ${e.message}")
            }.onSuccess { res ->
                if (res is UpdateAchievementResult.Error) {
                    Log.e("AppWidget", "UpdateAchievement error: ${res.message}")
                }
            }
        }

        // ウィジェット内の表示状態更新
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val updated = checkedEntries
                .filterNot { it.key == key }
                .plus(CheckedEntry(key, newChecked))

            prefs.toMutablePreferences().apply {
                this[PrefKeys.checkedJson] =
                    Json.encodeToString(ListSerializer(CheckedEntry.serializer()), updated)
                prefs
            }
        }

        AppWidget().update(context, glanceId)
    }
}