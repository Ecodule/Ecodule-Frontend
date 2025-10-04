package com.example.ecodule.ui.taskListContent.api

import android.util.Log
import com.example.ecodule.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

// 戻り値を表現するsealed classを定義
sealed class UpdateAchievementResult {
    data class Success(
        val co2Reduction: Double,
        val moneySaved: Double
    ) : UpdateAchievementResult()
    data class Error(val message: String) : UpdateAchievementResult()
}

@Serializable
data class UpdateAchievementRequest(
    val co2_reduction: Double,
    val money_saved: Double
)

object UpdateAchievement {
    private val client = OkHttpClient()

    suspend fun updateAchievement(
        accessToken: String,
        userId: String,
        co2: Double,
        money: Double,
    ): UpdateAchievementResult {
        return suspendCancellableCoroutine { continuation ->
            val url = BuildConfig.BASE_URL + "/$userId/simple_statistics"

            val updateAchievementData = UpdateAchievementRequest(
                co2_reduction = co2,
                money_saved = money
            )

            val jsonString = Json.encodeToString(updateAchievementData)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonString.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(UpdateAchievementResult.Error("ネットワークエラーが発生しました。"))
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        // 失敗時
                        if (!it.isSuccessful) {
                            val statusCode = response.code
                            val errorMessage = when (statusCode) {
                                in 400..499 -> {
                                    // 400番台はクライアント側のエラー (例: ID/パスワード間違い、リクエスト不正)
                                    "メールアドレスかパスワードが間違っています。\n間違いがないかご確認ください"
                                }
                                in 500..599 -> {
                                    // 500番台はサーバー側のエラー
                                    "サーバーエラーが発生しました。しばらくしてからもう一度お試しください。"
                                }
                                else -> {
                                    // その他のエラー
                                    "予期せぬエラーが発生しました。 status code: $statusCode"
                                }
                            }
                            Log.d("Ecodule", "Login failed with status code: $statusCode")

                            continuation.resume(UpdateAchievementResult.Error(errorMessage))
                            return
                        }

                        // レスポンスボディからJSONをパースしてトークンを取得
                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                val json = JSONObject(responseBody)
                                val result = UpdateAchievementResult.Success(
                                    co2Reduction = json.getDouble("total_co2_reduction"),
                                    moneySaved = json.getDouble("total_money_saved")
                                )
                                continuation.resume(result)
                            } catch (e: Exception) {
                                continuation.resume(UpdateAchievementResult.Error("レスポンスの解析に失敗しました。"))
                            }
                        } else {
                            continuation.resume(UpdateAchievementResult.Error("レスポンスボディが空です。"))
                        }
                    }
                }
            })
        }
    }
}