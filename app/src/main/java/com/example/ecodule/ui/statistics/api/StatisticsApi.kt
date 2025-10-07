package com.example.ecodule.ui.statistics.api

import android.util.Log
import com.example.ecodule.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

// 戻り値を表現するsealed classを定義（ユーザー統計）
sealed class GetUserStatisticsResult {
    data class Success(
        val co2Reduction: Double?,
        val moneySaved: Double?
    ) : GetUserStatisticsResult()
    data class Error(val message: String) : GetUserStatisticsResult()
}

// 戻り値を表現するsealed classを定義（全体統計）
sealed class GetOverallStatisticsResult {
    data class Success(
        val totalCo2Reduction: Double?,
        val totalMoneySaved: Double?
    ) : GetOverallStatisticsResult()
    data class Error(val message: String) : GetOverallStatisticsResult()
}

object StatisticsApi {
    private val client = OkHttpClient()

    /**
     * ユーザーの簡易統計を取得します。
     * GET {BASE_URL}/{userId}/simple_statistics
     * 認証必須: Bearer トークン
     */
    suspend fun getUserSimpleStatistics(
        accessToken: String,
        userId: String
    ): GetUserStatisticsResult {
        return suspendCancellableCoroutine { continuation ->
            val url = BuildConfig.BASE_URL + "/$userId/simple_statistics"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(GetUserStatisticsResult.Error("ネットワークエラーが発生しました。"))
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!it.isSuccessful) {
                            val statusCode = it.code
                            val errorMessage = when (statusCode) {
                                in 400..499 -> {
                                    // 400番台はクライアント側のエラー
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
                            Log.d("Ecodule", "GetUserSimpleStatistics failed with status code: $statusCode")
                            continuation.resume(GetUserStatisticsResult.Error(errorMessage))
                            return
                        }

                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                val json = JSONObject(responseBody)
                                val result = GetUserStatisticsResult.Success(
                                    co2Reduction = json.optDoubleOrNull("total_co2_reduction"),
                                    moneySaved = json.optDoubleOrNull("total_money_saved")
                                )
                                continuation.resume(result)
                            } catch (e: Exception) {
                                continuation.resume(GetUserStatisticsResult.Error("レスポンスの解析に失敗しました。"))
                            }
                        } else {
                            continuation.resume(GetUserStatisticsResult.Error("レスポンスボディが空です。"))
                        }
                    }
                }
            })
        }
    }

    /**
     * 全体統計を取得します。
     * GET {BASE_URL}/overall_statistics
     * 認証必須: Bearer トークン
     */
    suspend fun getOverallStatistics(
        accessToken: String
    ): GetOverallStatisticsResult {
        return suspendCancellableCoroutine { continuation ->
            val url = BuildConfig.BASE_URL + "/overall_statistics"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(GetOverallStatisticsResult.Error("ネットワークエラーが発生しました。"))
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!it.isSuccessful) {
                            val statusCode = it.code
                            val errorMessage = when (statusCode) {
                                in 400..499 -> {
                                    "メールアドレスかパスワードが間違っています。\n間違いがないかご確認ください"
                                }
                                in 500..599 -> {
                                    "サーバーエラーが発生しました。しばらくしてからもう一度お試しください。"
                                }
                                else -> {
                                    "予期せぬエラーが発生しました。 status code: $statusCode"
                                }
                            }
                            Log.d("Ecodule", "GetOverallStatistics failed with status code: $statusCode")
                            continuation.resume(GetOverallStatisticsResult.Error(errorMessage))
                            return
                        }

                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            try {
                                val json = JSONObject(responseBody)
                                val result = GetOverallStatisticsResult.Success(
                                    totalCo2Reduction = json.optDoubleOrNull("total_co2_reduction")
                                        ?: json.optDoubleOrNull("co2_reduction"),
                                    totalMoneySaved = json.optDoubleOrNull("total_money_saved")
                                        ?: json.optDoubleOrNull("money_saved")
                                )
                                continuation.resume(result)
                            } catch (e: Exception) {
                                continuation.resume(GetOverallStatisticsResult.Error("レスポンスの解析に失敗しました。"))
                            }
                        } else {
                            continuation.resume(GetOverallStatisticsResult.Error("レスポンスボディが空です。"))
                        }
                    }
                }
            })
        }
    }
}

// JSONObject向けの拡張関数: Doubleを安全に取り出す（null許容）
private fun JSONObject.optDoubleOrNull(key: String): Double? {
    return if (this.has(key) && !this.isNull(key)) {
        try {
            this.getDouble(key)
        } catch (_: Exception) {
            null
        }
    } else {
        null
    }
}