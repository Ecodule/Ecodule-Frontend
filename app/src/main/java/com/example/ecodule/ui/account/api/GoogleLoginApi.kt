package com.example.ecodule.ui.account.api

import android.util.Log
import com.example.ecodule.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
import kotlin.coroutines.resume

// 戻り値を表現するsealed classを定義
sealed class GoogleLoginResult {
    data class Success(
        val id: String,
        val email: String,
        val accessToken: String,
        val refreshToken: String,
        val expiresIn: Long
    ) : GoogleLoginResult()
    data class Error(val message: String) : GoogleLoginResult()
}

@Serializable
data class GoogleLoginRequest(
    val token: String
)

object GoogleLoginApi {
    private val client = OkHttpClient()

    suspend fun login(
        token: String,
    ): GoogleLoginResult {
        return suspendCancellableCoroutine { continuation ->
            val url = BuildConfig.BASE_URL + "/auth/google"

            val googleTokenData = GoogleLoginRequest(
                token = token
            )

            val jsonString = Json.encodeToString(googleTokenData)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonString.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(GoogleLoginResult.Error("ネットワークエラーが発生しました。"))
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        // 失敗時
                        if (!it.isSuccessful) {
                            val statusCode = response.code
                            val errorMessage = when (statusCode) {
                                in 400..499 -> {
                                    // 400番台はクライアント側のエラー (例: ID/パスワード間違い、リクエスト不正)
                                    "Googleアカウントの認証に失敗しました。もう一度お試しください"
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

                            continuation.resume(GoogleLoginResult.Error(errorMessage))
                            return
                        }

                        // レスポンスボディからJSONをパースしてトークンを取得
                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                val json = JSONObject(responseBody)
                                val result = GoogleLoginResult.Success(
                                    id = json.getString("id"),
                                    email = json.getString("email"),
                                    accessToken = json.getString("access_token"),
                                    refreshToken = json.getString("refresh_token"),
                                    expiresIn = json.getLong("expires_in")
                                )
                                continuation.resume(result)
                            } catch (e: Exception) {
                                continuation.resume(GoogleLoginResult.Error("レスポンスの解析に失敗しました。"))
                            }
                        } else {
                            continuation.resume(GoogleLoginResult.Error("レスポンスボディが空です。"))
                        }
                    }
                }
            })
        }
    }
}