package com.example.ecodule.ui.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
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

    Log.d("AppWidget", "ensureInitialized: galnceId=$glanceId")

    if (userId.isEmpty()) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                removeAllForWidget()
            }
        }
        return
    }

    val todayEvents = try {
        val today = LocalDate.now()
        // ★★★ ここを修正 ★★★
        // observeTasks(...).first() の代わりに getTasksOnce(...) を呼び出す
        val allTasks = ep.taskRepository().getTasksOnce(userId)
        allTasks.filter { it.startDate.toLocalDate() == today }
            .sortedBy { it.startDate }
    } catch (e: Exception) {
        emptyList()
    }

    val first = todayEvents.firstOrNull { !it.isCompleted }

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
                ep.checkedStateRepository().getCheckedStatesOnce(userId)
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

suspend fun reviewWidget(context: Context, glanceId: GlanceId) {
    val ep = EntryPointAccessors.fromApplication(context, AppWidgetEntryPoint::class.java)
    val user = ep.userRepository().user.first()
    val userId = user?.id ?: ""

    // 未ログイン状態は何もしない
    if (userId.isEmpty()) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                removeAllForWidget()
            }
        }
        return
    }

    // 現在イベントIDを取得
    var currentEventId: String? = null
    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
        currentEventId = prefs[PrefKeys.currentEventId]
        prefs
    }

    val todayEvents = try {
        val today = LocalDate.now()
        // ★★★ ここを修正 ★★★
        // observeTasks(...).first() の代わりに getTasksOnce(...) を呼び出す
        val allTasks = ep.taskRepository().getTasksOnce(userId)
        allTasks.filter { it.startDate.toLocalDate() == today }
            .sortedBy { it.startDate }
    } catch (e: Exception) {
        emptyList()
    }

    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
        prefs.toMutablePreferences().apply {
            val currentEventState = todayEvents.find { it.id == currentEventId }

            // 次のイベントがない場合は全て削除
            if (currentEventState == null) {
                removeAllForWidget()
                return@apply
            }

            if (currentEventState.isCompleted) {
                // 完了している場合も全て削除
                removeAllForWidget()
                return@apply
            }

            this[PrefKeys.currentEventJson] =
                Json.encodeToString(CurrentEventState.serializer(), CurrentEventState.from(currentEventState))
            this[PrefKeys.currentEventId] = currentEventState.id

            val ecoList = loadEcoActionsForEvent(ep, CurrentEventState.from(currentEventState))
            this[PrefKeys.ecoActionsJson] =
                Json.encodeToString(ListSerializer(EcoActionItem.serializer()), ecoList)

            val checkedMapForUser = runCatching {
                ep.checkedStateRepository().getCheckedStatesOnce(userId)
            }.getOrElse { emptyMap() }

            Log.d("AppWidget", "Checked states loaded for user $userId: ${checkedMapForUser.size} items")

            val entries = ecoList.map { item ->
                val key = buildCheckedKey(CurrentEventState.from(currentEventState), item)
                CheckedEntry(key, checked = checkedMapForUser[key] ?: false)
            }
            this[PrefKeys.checkedJson] =
                Json.encodeToString(ListSerializer(CheckedEntry.serializer()), entries)
        }
    }

    AppWidget().update(context, glanceId)
}

suspend fun reviewWidgetAndNext(context: Context, glanceId: GlanceId) {
    val ep = EntryPointAccessors.fromApplication(context, AppWidgetEntryPoint::class.java)
    val user = ep.userRepository().user.first()
    val userId = user?.id ?: ""

    // 現在イベントIDを取得
    var currentEventId: String? = null
    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
        currentEventId = prefs[PrefKeys.currentEventId]
        prefs
    }

    val todayEvents = try {
        val today = LocalDate.now()
        // ★★★ ここを修正 ★★★
        // observeTasks(...).first() の代わりに getTasksOnce(...) を呼び出す
        val allTasks = ep.taskRepository().getTasksOnce(userId)
        allTasks.filter { it.startDate.toLocalDate() == today }
            .sortedBy { it.startDate }
    } catch (e: Exception) {
        Log.e("AppWidget", "Failed to load tasks", e)
        emptyList()
    }

    val currentEvent: CalendarEvent? = todayEvents.firstOrNull { it.id == currentEventId }

    // 今日の予定が一つもない場合、状態をクリアして終了
    if (todayEvents.isEmpty()) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                removeAllForWidget()
            }
        }
        AppWidget().update(context, glanceId)
        return
    }

    // 次のイベントを決定
    // 現在のイベントが既に完了している場合は、次の未完了イベントを探す
    // 現在のイベントがまだ完了していない場合は、それを次のイベントとする
    Log.d("AppWidget", "Current event ID: $currentEventId, Current event: $currentEvent")

    val notCompletedFirst = todayEvents.firstOrNull { !it.isCompleted }

    Log.d("AppWidget", "First not completed eventDate: ${notCompletedFirst?.startDate} Current eventDate: ${currentEvent?.startDate}")

    val idx = todayEvents.indexOfFirst { it.id == currentEventId }
    val next: CalendarEvent? = when {
        // todayEventsにcurrentEventIdが存在しない場合、最初の未完了イベントを次のイベントとする
        (idx == -1 && !todayEvents.first().isCompleted) -> todayEvents.first()
        // currentEventIdがnullの場合、最初の未完了イベントを次のイベントとする
        (currentEvent == null) -> notCompletedFirst
        todayEvents.all { it.isCompleted } -> null
        // 現在のイベントが未完了かつ一番早い場合、それを次のイベントとする
        (!currentEvent.isCompleted && notCompletedFirst!!.startDate >= currentEvent.startDate) -> currentEvent
        else -> notCompletedFirst
    }

    Log.d("AppWidget", "Next event determined: $next")

    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
        prefs.toMutablePreferences().apply {
            if (next == null) {
                removeAllForWidget()
                return@apply
            }
            val eventState = next
            this[PrefKeys.currentEventJson] =
                Json.encodeToString(
                    CurrentEventState.serializer(),
                    CurrentEventState.from(eventState)
                )
            this[PrefKeys.currentEventId] = eventState.id

            val ecoList = loadEcoActionsForEvent(ep, CurrentEventState.from(next))
            this[PrefKeys.ecoActionsJson] =
                Json.encodeToString(ListSerializer(EcoActionItem.serializer()), ecoList)

            val checkedMapForUser = runCatching {
                ep.checkedStateRepository().getCheckedStatesOnce(userId)
            }.getOrElse { emptyMap() }

            Log.d(
                "AppWidget",
                "Checked states loaded for user $userId: ${checkedMapForUser.size} items"
            )

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