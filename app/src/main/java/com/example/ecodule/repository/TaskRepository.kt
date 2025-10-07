package com.example.ecodule.repository

import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import kotlinx.coroutines.flow.StateFlow

interface TaskRepository {
    /**
     * ユーザーIDに紐づくタスク一覧を監視します。
     */
    fun observeTasks(userId: String): StateFlow<List<CalendarEvent>>

    suspend fun getTasksOnce(userId: String): List<CalendarEvent>

    /**
     * 新しいタスクを追加します。
     */
    suspend fun addTask(userId: String, newTask: CalendarEvent)

    /**
     * 指定したイベントIDのタスクを削除します。
     */
    suspend fun deleteTask(userId: String, eventId: String)

    /**
     * ユーザーIDに紐づくすべてのタスクを削除します。
     */
    suspend fun clearTasks(userId: String)
}
