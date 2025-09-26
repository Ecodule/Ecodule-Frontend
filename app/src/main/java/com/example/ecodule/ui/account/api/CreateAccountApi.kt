package com.example.ecodule.ui.account.api

import android.util.Log
import com.example.ecodule.BuildConfig
import com.example.ecodule.api.TokenRefresh.refreshToken
import com.example.ecodule.api.TokenRefreshRequest
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
sealed class CreateAccountResult {
    data class Success(
        val id: String,
        val message: String,
        val isActive: Boolean,
        val createdAt: String,
    ) : CreateAccountResult()
    data class Error(val message: String) : CreateAccountResult()
}

@Serializable
data class CreateAccountRequest(
    val email: String,
    val password: String
)

object UserCreateApi {
    private val client = OkHttpClient()

    suspend fun userCreate(
        email: String,
        password: String,
    ): CreateAccountResult {
        return suspendCancellableCoroutine { continuation ->
            val url = BuildConfig.BASE_URL + "/users/create"

            val createAccountData = CreateAccountRequest(
                email = email,
                password = password
            )

            val jsonString = Json.encodeToString(createAccountData)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonString.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(CreateAccountResult.Error("ネットワークエラー: ${e.message}"))
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        // レスポンスボディからJSONをパース
                        val responseBody = it.body?.string()

                        // 失敗時
                        if (!it.isSuccessful) {
                            val statusCode = response.code
                            val errorMessage = when (statusCode) {
                                400 -> {
                                    // 400番台はクライアント側のエラー (例: ID/パスワード間違い、リクエスト不正)
                                    "既に登録されているメールアドレスです。別のメールアドレスをご利用ください。"
                                }
                                in 401..499 -> {
                                    // 400番台はクライアント側のエラー (例: ID/パスワード間違い、リクエスト不正)
                                    "メールアドレスとパスワードを正しい形式でご入力ください"
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
                            Log.d("EcoduleCreateAccount", "Sign up failed with status code: $statusCode")
                            Log.d("EcoduleCreateAccount", "Sign up failed with status code: $responseBody")
                            return
                        }

                        if (responseBody != null) {
                            try {
                                val json = JSONObject(responseBody)
                                val result = CreateAccountResult.Success(
                                    id = json.getString("id"),
                                    message = json.getString("message"),
                                    isActive = json.getBoolean("is_active"),
                                    createdAt = json.getString("created_at"),
                                )
                                continuation.resume(result)
                            } catch (e: Exception) {
                                continuation.resume(CreateAccountResult.Error("レスポンスの解析に失敗しました。"))
                            }
                        } else {
                            continuation.resume(CreateAccountResult.Error("レスポンスボディが空です。"))
                        }
                    }
                }
            })
        }

    }
}