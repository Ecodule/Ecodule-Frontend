package com.example.ecodule.di

import android.content.Context
import androidx.core.content.contentValuesOf
import com.example.ecodule.repository.EcoActionRepository
import com.example.ecodule.repository.TaskRepository
import com.example.ecodule.repository.UserRepository
import com.example.ecodule.repository.datastore.DataStoreEcoActionRepository
import com.example.ecodule.repository.datastore.DataStoreTaskRepository
import com.example.ecodule.repository.datastore.DataStoreUserRepository
import com.example.ecodule.repository.datastore.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // アプリ全体で有効なモジュール
object AppModule {

    @Provides
    @Singleton // アプリ内で常に同じインスタンスを使用する
    fun provideUserRepository(
        @ApplicationContext context: Context
    ): UserRepository {
        return DataStoreUserRepository(context)
    }

    @Provides
    @Singleton // アプリ内で常に同じインスタンスを使用する
    fun provideTaskRepository(
        @ApplicationContext context: Context
    ): TaskRepository {
        return DataStoreTaskRepository(context)
    }

    @Provides
    @Singleton // アプリ内で常に同じインスタンスを使用する
    fun provideEcoActionRepository(
        @ApplicationContext context: Context
    ): EcoActionRepository {
        return DataStoreEcoActionRepository(context)
    }

    // 以下を追加
    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context
    ): TokenManager {
        return TokenManager(context)
    }
}
