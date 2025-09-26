package com.example.ecodule.ui.account.api

import android.util.Log
import com.example.ecodule.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

// 戻り値を表現するsealed classを定義
sealed class LoginResult {
    data class Success(
        val id: String,
        val accessToken: String,
        val refreshToken: String,
        val expiresIn: Long
    ) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

object LoginApi {
    private val client = OkHttpClient()

    suspend fun login(
        email: String,
        password: String,
    ): LoginResult {
        return suspendCancellableCoroutine { continuation ->
            val url = BuildConfig.BASE_URL + "/auth/login"

            val formBody = FormBody.Builder()
                .add("username", email)
                .add("password", password)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(LoginResult.Error("ネットワークエラー: ${e.message}"))
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

                            continuation.resume(LoginResult.Error(errorMessage))
                            return
                        }

                        // レスポンスボディからJSONをパースしてトークンを取得
                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                val json = JSONObject(responseBody)
                                val result = LoginResult.Success(
                                    id = json.getString("id"),
                                    accessToken = json.getString("access_token"),
                                    refreshToken = json.getString("refresh_token"),
                                    expiresIn = json.getLong("expires_in")
                                )
                                continuation.resume(result)
                            } catch (e: Exception) {
                                continuation.resume(LoginResult.Error("レスポンスの解析に失敗しました。"))
                            }
                        } else {
                            continuation.resume(LoginResult.Error("レスポンスボディが空です。"))
                        }
                    }
                }
            })
        }

    }
}