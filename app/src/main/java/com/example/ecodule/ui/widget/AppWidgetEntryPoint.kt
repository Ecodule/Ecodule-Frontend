package com.example.ecodule.ui.widget

import com.example.ecodule.repository.CheckedStateRepository
import com.example.ecodule.repository.EcoActionRepository
import com.example.ecodule.repository.TaskRepository
import com.example.ecodule.repository.UserRepository
import com.example.ecodule.repository.datastore.TokenManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppWidgetEntryPoint {
    fun taskRepository(): TaskRepository
    fun ecoActionRepository(): EcoActionRepository
    fun checkedStateRepository(): CheckedStateRepository
    fun tokenManager(): TokenManager
    fun userRepository(): UserRepository
}