package com.example.ecodule.ui.account.api

import android.R.id.message
import android.util.Log
import com.example.ecodule.BuildConfig
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

// 戻り値を表現するsealed classを定義
sealed class AccountCreateResult {
    data class Success(
        val email: String,
        val message: String,
        val isActive: Boolean,
        val createdAt: String,
    ) : AccountCreateResult()
    data class Error(val message: String) : AccountCreateResult()
}

@Serializable
data class AccountCreateRequest(
    val email: String,
    val password: String
)

object AccountCreateApi {
    private val client = OkHttpClient()

    suspend fun accountCreate(
        email: String,
        password: String,
    ): AccountCreateResult {
        return suspendCancellableCoroutine { continuation ->
            val url = BuildConfig.BASE_URL + "/users/create"

            val accountCreateData = AccountCreateRequest(
                email = email,
                password = password
            )

            val jsonString = Json.encodeToString(accountCreateData)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonString.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(AccountCreateResult.Error("ネットワークエラー: ${e.message}"))
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
                                    Log.d("EcoduleAccountCreate", "400 error")
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
                            Log.d("EcoduleAccountCreate", "Sign up failed with status code: $statusCode")
                            Log.d("EcoduleAccountCreate", "Sign up failed with status code: $responseBody")

                            continuation.resume(AccountCreateResult.Error(errorMessage))
                            return
                        }

                        if (responseBody != null) {
                            Log.d("AccountCreate", "now response body: $responseBody")
                            try {
                                val jsonArray = JSONArray(responseBody)

                                // 3. 配列の最初の要素（インデックス0）をJSONObjectとして取得
                                val userDataObject: JSONObject = jsonArray.getJSONObject(0)

                                // 4. 配列の2番目の要素（インデックス1）をJSONObjectとして取得
                                val messageObject: JSONObject = jsonArray.getJSONObject(1)
                                val result = AccountCreateResult.Success(
                                    email = userDataObject.getString("email"),
                                    message = messageObject.getString("message"),
                                    isActive = userDataObject.getBoolean("is_active"),
                                    createdAt = userDataObject.getString("created_at"),
                                )
                                continuation.resume(result)
                            } catch (e: Exception) {
                                continuation.resume(AccountCreateResult.Error("レスポンスの解析に失敗しました。"))
                            }
                        } else {
                            continuation.resume(AccountCreateResult.Error("レスポンスボディが空です。"))
                        }
                    }
                }
            })
        }

    }
}