package com.example.ecodule.di

import com.example.ecodule.repository.TaskRepository
import com.example.ecodule.repository.datastore.DataStoreTaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {
    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        dataStoreTaskRepository: DataStoreTaskRepository
    ): TaskRepository
}