package com.ecodule.android.security

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.GeneralSecurityException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "ecodule_secure_tokens")

@Singleton
class SecureTokenManager @Inject constructor(
    private val context: Context
) {
    private lateinit var aead: Aead
    private val dataStore = context.tokenDataStore

    private val masterKeyUri = "android-keystore://ecodule_master_key"
    private val keysetName = "ecodule_token_keyset"
    private val prefsFileName = "ecodule_encrypted_prefs"

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("encrypted_access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("encrypted_refresh_token")
        private val DEVICE_ID_KEY = stringPreferencesKey("device_id")
        private val USER_DATA_KEY = stringPreferencesKey("user_data")
    }

    @Serializable
    data class SecureToken(
        val encryptedToken: String,
        val tokenType: String,
        val createdAt: Long,
        val expiresAt: Long,
        val userId: String,
        val deviceId: String
    )

    @Serializable
    data class UserData(
        val userId: String,
        val email: String,
        val name: String,
        val lastLoginAt: Long
    )

    suspend fun initialize() {
        try {
            AeadConfig.register()

            val keysetHandle = AndroidKeysetManager.Builder()
                .withSharedPref(context, keysetName, prefsFileName)
                .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
                .withMasterKeyUri(masterKeyUri)
                .build()
                .keysetHandle

            aead = keysetHandle.getPrimitive(Aead::class.java)
            ensureDeviceId()

        } catch (e: GeneralSecurityException) {
            throw SecurityException("セキュリティ初期化に失敗しました: ${e.message}", e)
        }
    }

    /**
     * ログイン情報を一括保存
     */
    suspend fun saveLoginData(
        userId: String,
        email: String,
        name: String,
        accessToken: String,
        refreshToken: String,
        accessExpiresIn: Long = 3600,
        refreshExpiresIn: Long = 2592000
    ) {
        val userData = UserData(
            userId = userId,
            email = email,
            name = name,
            lastLoginAt = System.currentTimeMillis()
        )

        saveUserData(userData)
        saveToken(userId, accessToken, ACCESS_TOKEN_KEY, "ACCESS_TOKEN", accessExpiresIn)
        saveToken(userId, refreshToken, REFRESH_TOKEN_KEY, "REFRESH_TOKEN", refreshExpiresIn)
    }

    private suspend fun saveUserData(userData: UserData) {
        val userDataJson = Json.encodeToString(userData)
        dataStore.edit { preferences ->
            preferences[USER_DATA_KEY] = userDataJson
        }
    }

    private suspend fun saveToken(
        userId: String,
        token: String,
        key: Preferences.Key<String>,
        tokenType: String,
        expiresInSeconds: Long
    ) {
        try {
            val deviceId = getDeviceId()
            val associatedData = "$userId:$tokenType:$deviceId"

            val encryptedTokenBytes = aead.encrypt(token.toByteArray(), associatedData.toByteArray())
            val base64EncryptedToken = Base64.encodeToString(encryptedTokenBytes, Base64.NO_WRAP)

            val now = System.currentTimeMillis()
            val expiresAt = now + (expiresInSeconds * 1000)

            val secureToken = SecureToken(
                encryptedToken = base64EncryptedToken,
                tokenType = tokenType,
                createdAt = now,
                expiresAt = expiresAt,
                userId = userId,
                deviceId = deviceId
            )

            val tokenJson = Json.encodeToString(secureToken)
            dataStore.edit { preferences ->
                preferences[key] = tokenJson
            }

        } catch (e: GeneralSecurityException) {
            throw SecurityException("トークンの暗号化に失敗しました: ${e.message}", e)
        }
    }

    /**
     * アクセストークンを取得
     */
    suspend fun getAccessToken(): String? {
        return getToken(ACCESS_TOKEN_KEY, "ACCESS_TOKEN")
    }

    /**
     * リフレッシュトークンを取得
     */
    suspend fun getRefreshToken(): String? {
        return getToken(REFRESH_TOKEN_KEY, "REFRESH_TOKEN")
    }

    private suspend fun getToken(key: Preferences.Key<String>, tokenType: String): String? {
        try {
            val tokenJson = dataStore.data.map { preferences ->
                preferences[key]
            }.first() ?: return null

            val secureToken = Json.decodeFromString<SecureToken>(tokenJson)

            // 有効期限チェック
            if (System.currentTimeMillis() > secureToken.expiresAt) {
                deleteTokenByKey(key)
                return null
            }

            // デバイスIDチェック
            val currentDeviceId = getDeviceId()
            if (secureToken.deviceId != currentDeviceId) {
                return null
            }

            // トークンを復号化
            val associatedData = "${secureToken.userId}:$tokenType:${secureToken.deviceId}"
            val encryptedToken = Base64.decode(secureToken.encryptedToken, Base64.NO_WRAP)
            val decryptedToken = aead.decrypt(encryptedToken, associatedData.toByteArray())

            return String(decryptedToken)

        } catch (e: Exception) {
            throw SecurityException("トークンの復号化に失敗しました: ${e.message}", e)
        }
    }

    /**
     * ユーザーデータを取得
     */
    suspend fun getUserData(): UserData? {
        return try {
            val userDataJson = dataStore.data.map { preferences ->
                preferences[USER_DATA_KEY]
            }.first() ?: return null

            Json.decodeFromString<UserData>(userDataJson)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 認証状態のFlow
     */
    fun getAuthenticationStateFlow(): Flow<AuthenticationState> {
        return dataStore.data.map { preferences ->
            val userData = preferences[USER_DATA_KEY]
            val hasAccessToken = preferences[ACCESS_TOKEN_KEY] != null
            val hasRefreshToken = preferences[REFRESH_TOKEN_KEY] != null

            when {
                userData != null && hasAccessToken -> {
                    val user = Json.decodeFromString<UserData>(userData)
                    AuthenticationState.Authenticated(user)
                }
                userData != null && hasRefreshToken -> {
                    val user = Json.decodeFromString<UserData>(userData)
                    AuthenticationState.RefreshRequired(user)
                }
                else -> AuthenticationState.Unauthenticated
            }
        }
    }

    /**
     * すべてのデータを削除
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
        ensureDeviceId()
    }

    private suspend fun deleteTokenByKey(key: Preferences.Key<String>) {
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    private suspend fun getDeviceId(): String {
        return dataStore.data.map { preferences ->
            preferences[DEVICE_ID_KEY]
        }.first() ?: throw IllegalStateException("Device ID not initialized")
    }

    private suspend fun ensureDeviceId() {
        val existingDeviceId = dataStore.data.map { preferences ->
            preferences[DEVICE_ID_KEY]
        }.first()

        if (existingDeviceId == null) {
            val newDeviceId = UUID.randomUUID().toString()
            dataStore.edit { preferences ->
                preferences[DEVICE_ID_KEY] = newDeviceId
            }
        }
    }
}

sealed class AuthenticationState {
    object Unauthenticated : AuthenticationState()
    data class Authenticated(val userData: SecureTokenManager.UserData) : AuthenticationState()
    data class RefreshRequired(val userData: SecureTokenManager.UserData) : AuthenticationState()
}