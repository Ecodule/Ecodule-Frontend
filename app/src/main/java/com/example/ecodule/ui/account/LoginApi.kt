package com.example.ecodule.ui.account

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

object LoginApi {
    private val client = OkHttpClient()

    fun login(email: String, password: String, callback: (success: Boolean, message: String?) -> Unit) {
        val url = "https://ecodule.ddns.net/auth/login" // ← サーバーのAPIエンドポイント
        Log.d("asdfghjkqwertyuiopecodule", "aaa")

        val json = JSONObject()
        json.put("email", email)
        json.put("password", password)

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "ネットワークエラー: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // ここでレスポンス内容（例: トークンなど）を取得して処理
                    callback(true, null)
                } else {
                    callback(false, "認証失敗: ${response.message}")
                }
            }
        })
    }
}
