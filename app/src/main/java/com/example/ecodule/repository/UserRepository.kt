package com.example.ecodule.repository

import com.example.ecodule.ui.sharedViewModel.UserData
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    // ユーザー情報をStateFlowで提供
    val user: StateFlow<UserData?>

    suspend fun saveUser(id: String, email: String)
    suspend fun clearUser()
}