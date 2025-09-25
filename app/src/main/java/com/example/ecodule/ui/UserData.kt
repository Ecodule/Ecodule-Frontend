package com.example.ecodule.ui

import kotlinx.serialization.Serializable

@Serializable // このアノテーションを追加
data class UserData(
    val id: String,
    val email: String,
)