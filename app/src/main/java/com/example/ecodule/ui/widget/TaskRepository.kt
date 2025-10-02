package com.example.ecodule.ui.widget

import com.example.ecodule.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

//object TaskRepository {
//    private val client = OkHttpClient()
//
//    // 例: 今日のタスクを返すエンドポイントを想定
//    suspend fun fetchTodayTasks(): List<AppWidget.Task> {
//        val url = "${BuildConfig.BASE_URL}/api/tasks/today" // 実 API に合わせて変更
//        val req = Request.Builder().url(url).build()
//
//        return runCatching {
//            client.newCall(req).execute().use { resp ->
//                if (!resp.isSuccessful) return emptyList()
//                val body = resp.body?.string().orEmpty()
//                // 受け取る JSON を [{id,title,isDone}] の形に合わせる
//                Json.decodeFromString(body)
//            }
//        }.getOrElse { emptyList() }
//    }
//}