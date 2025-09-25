package com.example.ecodule.repository

interface UserRepository {
    suspend fun save(body: String)

    suspend fun load(): String
}