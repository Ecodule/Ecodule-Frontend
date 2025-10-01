package com.example.ecodule.ui.CalendarContent.model

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodule.repository.datastore.DataStoreTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: DataStoreTaskRepository
) : ViewModel() {
    // ユーザーIDはStateFlow等で外部からセット/購読する形にする（例：UserRepositoryから取得）
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events : StateFlow<List<CalendarEvent>> = _events

    // 追加: 今日のイベントだけ絞り込むFlow
    val todayEvents: StateFlow<List<CalendarEvent>> = events
        .map { list ->
            val today = LocalDate.now()
            list.filter { it.startDate.dayOfMonth == today.dayOfMonth && it.startDate.monthValue == today.monthValue }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // ユーザーIDをセットしたら購読開始
    fun setUserId(userId: String) {
        _userId.value = userId
        viewModelScope.launch {
            repository.observeTasks(userId).collect {
                _events.value = it
            }
            // ダミー予定を常に追加（初回のみ追加したい場合はこのままでOK）
            val now = LocalDateTime.now()
            val currentEvents = repository.observeTasks(userId).value
        }
    }

    // DataStoreに予定を追加
    fun addEvent(event: CalendarEvent) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            repository.addTask(uid, event)
        }
    }

    fun addEvent(
        title: String,
        category: String,
        description: String,
        allDay: Boolean,
        startDate: Date,
        endDate: Date,
        repeatOption: String,
        memo: String,
        notificationMinutes: Int,
        repeatGroupId: String? = null
    ) {
        val categoryColor = getCategoryColor(category)
        val startDateTime = LocalDateTime.ofInstant(startDate.toInstant(), java.time.ZoneId.systemDefault())
        val endDateTime = LocalDateTime.ofInstant(endDate.toInstant(), java.time.ZoneId.systemDefault())

        val event = CalendarEvent(
            id = UUID.randomUUID().toString(),
            label = title,
            startDate = startDateTime,
            endDate = endDateTime,
            category = category,
            allDay = allDay,
            repeatOption = repeatOption,
            memo = memo,
            notificationMinutes = notificationMinutes,
            repeatGroupId = repeatGroupId,
            color = categoryColor // ← ここを追加!
        )

        addEvent(event)
    }

    fun updateEvent(
        id: String,
        title: String,
        category: String,
        description: String,
        allDay: Boolean,
        startDate: Date,
        endDate: Date,
        repeatOption: String,
        memo: String,
        notificationMinutes: Int
    ) {
        val categoryColor = getCategoryColor(category)
        val startDateTime = LocalDateTime.ofInstant(startDate.toInstant(), java.time.ZoneId.systemDefault())
        val endDateTime = LocalDateTime.ofInstant(endDate.toInstant(), java.time.ZoneId.systemDefault())

        val updatedEvent = CalendarEvent(
            id = id,
            label = title,
            color = categoryColor, // ← ここを追加!
            startDate = startDateTime,
            endDate = endDateTime,
            category = category,
            allDay = allDay,
            repeatOption = repeatOption,
            memo = memo,
            notificationMinutes = notificationMinutes
        )
        val uid = _userId.value ?: return
        viewModelScope.launch {
            repository.deleteTask(uid, id)
            repository.addTask(uid, updatedEvent)

            val updatedEvents = repository.observeTasks(uid)
            Log.d("FromTaskViewModel", "data is $updatedEvents")
        }
    }

    // 繰り返しグループ全体編集
    fun updateEventsByRepeatGroup(
        repeatGroupId: String,
        title: String,
        category: String,
        description: String,
        allDay: Boolean,
        startDate: Date,
        endDate: Date,
        repeatOption: String,
        memo: String,
        notificationMinutes: Int
    ) {
        val eventsToUpdate = _events.value.filter { it.repeatGroupId == repeatGroupId }
        eventsToUpdate.forEach { event ->
            updateEvent(
                event.id, title, category, description, allDay,
                startDate, endDate, repeatOption, memo, notificationMinutes
            )
        }
    }

    // 予定削除
    fun deleteEvent(id: String) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            repository.deleteTask(uid, id)
        }
    }

    // IDで1件取得
    fun getEventById(id: String): CalendarEvent? {
        return _events.value.find { it.id == id }
    }

    private fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
        return when (category) {
            "ゴミ出し" -> androidx.compose.ui.graphics.Color(0xFFB3E6FF)
            "通勤/通学" -> androidx.compose.ui.graphics.Color(0xFFFFD2C5)
            "外出" -> androidx.compose.ui.graphics.Color(0xFFE4EFCF)
            "買い物" -> androidx.compose.ui.graphics.Color(0xFFC9E4D7)
            else -> androidx.compose.ui.graphics.Color(0xFF81C784)
        }
    }
}
