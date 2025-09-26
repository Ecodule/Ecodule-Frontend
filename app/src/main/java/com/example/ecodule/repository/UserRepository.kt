package com.example.ecodule.repository

import com.example.ecodule.repository.UserData
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface UserRepository {
    // ユーザー情報をStateFlowで提供
    val user: StateFlow<UserData?>

    suspend fun saveUser(id: String, email: String)
    suspend fun saveUserWithToken(id: String, email: String, accessToken: String, refreshToken: String, expiresIn: Long)
    suspend fun clearUser()
}

@Serializable // このアノテーションを追加
data class UserData(
    val id: String,
    val email: String,
)