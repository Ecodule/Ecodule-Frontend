package com.example.ecodule.ui.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.ecodule.ui.util.TokenRefresh // 以前作成したAPIクラス
import com.example.ecodule.ui.account.model.UserViewModel

/**
 * EncryptedSharedPreferencesを使用してアクセストークンを安全に管理するクラス
 */
class TokenManager(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_auth_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // 変更点: TokenManagerのコンストラクタでユーザーのメールアドレスを受け取る


    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        // キー名を「有効期間（秒）」であることがわかるように変更 (expires_in)
        private const val KEY_EXPIRES_IN = "expires_in"
        // 変更点: トークンを保存した時刻を記録するキーを追加
        private const val KEY_TOKEN_SAVED_AT = "token_saved_at"
    }

    /**
     * トークンと有効期間を保存する
     * @param accessToken アクセストークン
     * @param refreshToken リフレッシュトークン
     * @param expiresIn 有効期間（秒）
     */
    fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_EXPIRES_IN, expiresIn) // 有効期間（秒）を保存
            // 変更点: 現在の時刻（ミリ秒）を保存
            .putLong(KEY_TOKEN_SAVED_AT, System.currentTimeMillis())
            .apply()
    }

    /**
     * 保存されているアクセストークンを取得する。
     * 期限切れの場合は、リフレッシュトークンで新しいトークンを非同期に取得する。
     * @return 有効なアクセストークン。取得失敗時はnull
     */
    suspend fun getAccessToken(userEmail: String): String? {
        if (isTokenExpired()) {
            val refreshToken = getRefreshToken() ?: return null

            val refreshResult = TokenRefresh.refreshToken(refreshToken, userEmail)

            return if (refreshResult != null) {
                saveTokens(
                    refreshResult.accessToken,
                    refreshResult.refreshToken,
                    refreshResult.expiresIn
                )
                refreshResult.accessToken
            } else {
                deleteTokens()
                null
            }
        } else {
            return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        }
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * 変更点: 新しいロジックでトークンが期限切れかどうかを判定する
     * @return 期限切れならtrue
     */
    private fun isTokenExpired(): Boolean {
        // 保存した時刻（ミリ秒）を取得
        val savedAt = sharedPreferences.getLong(KEY_TOKEN_SAVED_AT, 0)
        // 有効期間（秒）を取得
        val expiresInSeconds = sharedPreferences.getLong(KEY_EXPIRES_IN, 0)

        // どちらかの値が保存されていなければ、無効（期限切れ）とみなす
        if (savedAt == 0L || expiresInSeconds == 0L) {
            return true
        }

        // 有効期限の絶対的なタイムスタンプを計算
        // 保存時刻（ミリ秒） + 有効期間（ミリ秒に変換）
        val expiresAt = savedAt + (expiresInSeconds * 1000)

        // 現在時刻が、計算した有効期限を過ぎていたらtrue
        return System.currentTimeMillis() >= expiresAt
    }

    /**
     * 保存されている全てのトークン情報を削除する
     */
    fun deleteTokens() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EXPIRES_IN)
            .remove(KEY_TOKEN_SAVED_AT) // 変更点: 保存時刻も削除
            .apply()
    }
}