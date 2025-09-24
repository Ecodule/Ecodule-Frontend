package com.ecodule.android.repository

import com.ecodule.android.api.AuthApiService
import com.ecodule.android.model.LoginRequest
import com.ecodule.android.model.LoginResponse
import com.ecodule.android.security.AuthenticationState
import com.ecodule.android.security.SecureTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: SecureTokenManager
) {

    fun getAuthenticationState(): Flow<AuthenticationState> {
        return tokenManager.getAuthenticationStateFlow()
    }

    suspend fun login(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val loginRequest = LoginRequest(email = email, password = password)
            val response = authApiService.login(loginRequest)

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!

                tokenManager.saveLoginData(
                    userId = loginResponse.userId,
                    email = email,
                    name = loginResponse.name,
                    accessToken = loginResponse.accessToken,
                    refreshToken = loginResponse.refreshToken,
                    accessExpiresIn = loginResponse.expiresIn,
                    refreshExpiresIn = loginResponse.refreshExpiresIn
                )

                Result.success(Unit)
            } else {
                Result.failure(Exception("ログインに失敗しました: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
            if (accessToken != null) {
                try {
                    authApiService.logout("Bearer $accessToken")
                } catch (e: Exception) {
                    // サーバーサイドログアウトが失敗してもローカルは削除
                }
            }

            tokenManager.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.clearAll()
            Result.failure(e)
        }
    }

    suspend fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }

    suspend fun getUserData() = tokenManager.getUserData()
}