package com.example.ecodule.ui.account.api

import android.util.Log
import com.example.ecodule.BuildConfig
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object LoginApi {
    private val client = OkHttpClient()

    fun login(
        email: String,
        password: String,
        callback: (
            success: Boolean,
            message: String?,
            id: String?,
            accessToken: String?,
            refreshToken: String?,
            expiresIn: Long? ) -> Unit
    ) {
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
                // accessTokenはnullで渡す
                callback(false, "ネットワークエラー: ${e.message}", null, null, null, null)
                Log.d("Ecodule", e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    // 失敗時
                    if (!response.isSuccessful) {
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
                        callback(false, errorMessage, null, null, null, null)
                        return
                    }

                    // レスポンスボディからJSONをパースしてトークンを取得
                    val responseBody = it.body?.string()
                    if (responseBody != null) {
                        try {
                            val json = JSONObject(responseBody)
                            val id = json.getString("id")
                            val accessToken = json.getString("access_token")
                            val refreshToken = json.getString("refresh_token")
                            val expiresIn = json.getString("expires_in")

                            // 成功時にトークンを渡す
                            callback(true, "ログインに成功しました。", id, accessToken, refreshToken, expiresIn.toLong())
                        } catch (e: Exception) {
                            // JSONのパース失敗時
                            callback(false, "レスポンスの解析に失敗しました。",null, null, null, null)
                        }
                    } else {
                        // レスポンスボディが空の場合
                        callback(false, "レスポンスが空です。",null, null, null, null)
                    }
                }
            }
        })
    }
}