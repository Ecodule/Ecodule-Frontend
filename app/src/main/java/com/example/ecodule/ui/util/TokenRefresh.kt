
package com.example.ecodule.ui.util

import com.example.ecodule.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume

// APIレスポンスを格納するデータクラス
data class TokenRefreshResult(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

@Serializable
data class TokenRefreshRequest(
    val refresh_token: String,
    val email: String
)

object TokenRefresh {
    private val client = OkHttpClient()

    // suspend関数として定義
    suspend fun refreshToken(refreshToken: String, email: String): TokenRefreshResult? {
        // suspendCancellableCoroutineでコールバックをsuspend関数に変換
        return suspendCancellableCoroutine { continuation ->
            val url = BuildConfig.BASE_URL + "/auth/refresh" // 実際のリフレッシュエンドポイントに合わせる

            val refreshData = TokenRefreshRequest(
                refresh_token = refreshToken,
                email = email
            )

            val jsonString = Json.encodeToString(refreshData)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonString.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // エラー時はnullを返す
                    continuation.resume(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!it.isSuccessful) {
                            continuation.resume(null)
                            return
                        }

                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                val json = JSONObject(responseBody)
                                val result = TokenRefreshResult(
                                    accessToken = json.getString("access_token"),
                                    refreshToken = json.getString("refresh_token"), // 新しいリフレッシュトークンが返される場合
                                    expiresIn = json.getLong("expires_in")
                                )
                                // 成功時に結果を返す
                                continuation.resume(result)
                            } catch (e: Exception) {
                                continuation.resume(null)
                            }
                        } else {
                            continuation.resume(null)
                        }
                    }
                }
            })

            // コルーチンがキャンセルされたらAPIコールもキャンセルする
            continuation.invokeOnCancellation {
                client.newCall(request).cancel()
            }
        }
    }
}