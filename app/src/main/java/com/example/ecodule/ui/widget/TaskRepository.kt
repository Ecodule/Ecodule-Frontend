package com.example.ecodule.ui.widget

import com.example.ecodule.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object TaskRepository {
    private val client = OkHttpClient()
    private val json = "application/json; charset=utf-8".toMediaType()

    suspend fun markTaskDone(taskId: String): Boolean {
        val url = "${BuildConfig.BASE_URL}/api/tasks/$taskId/done" // 実 API に合わせて変更
        val body = Json.encodeToString(DonePayload(taskId)).toRequestBody(json)
        val req = Request.Builder().url(url).post(body).build()
        return runCatching { client.newCall(req).execute().use { it.isSuccessful } }.getOrElse { false }
    }

    suspend fun markTasksDone(taskIds: List<String>): Boolean {
        if (taskIds.isEmpty()) return true
        val url = "${BuildConfig.BASE_URL}/api/tasks/done" // 実 API に合わせて変更
        val body = Json.encodeToString(DoneListPayload(taskIds)).toRequestBody(json)
        val req = Request.Builder().url(url).post(body).build()
        return runCatching { client.newCall(req).execute().use { it.isSuccessful } }.getOrElse { false }
    }

    @Serializable private data class DonePayload(val taskId: String)
    @Serializable private data class DoneListPayload(val taskIds: List<String>)
}