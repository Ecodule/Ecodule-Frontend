package com.example.ecodule.ui.account

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

object LoginApi {
    private val client = OkHttpClient()

    fun login(email: String, password: String, callback: (success: Boolean, message: String?) -> Unit) {
        val url = "https://ecodule.ddns.net/auth/login"

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
                callback(false, "ネットワークエラー: ${e.message}")
                Log.d("Ecodule", e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                // responseオブジェクトが利用可能になった時に実行する
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    // headers
                    for ((name, value) in response.headers) {
                        Log.d("Ecodule", "$name: $value")
                    }

                    // レスポンス処理
                    Log.d("Ecodule", response.body!!.string())
                }
            }
        })
    }
}
