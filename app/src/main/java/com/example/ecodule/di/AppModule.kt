package com.example.ecodule.di

import android.content.Context
import androidx.core.content.contentValuesOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.ecodule.repository.EcoActionRepository
import com.example.ecodule.repository.TaskRepository
import com.example.ecodule.repository.UserRepository
import com.example.ecodule.repository.datastore.DataStoreCheckedStateRepository
import com.example.ecodule.repository.datastore.DataStoreEcoActionRepository
import com.example.ecodule.repository.datastore.DataStoreTaskRepository
import com.example.ecodule.repository.datastore.DataStoreUserRepository
import com.example.ecodule.repository.datastore.TokenManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

// DataStoreのファイル名を定義
private const val TASK_PREFERENCES_NAME = "tasks"
// DataStoreのインスタンスをContextの拡張プロパティとして定義
private val Context.taskStore: DataStore<Preferences> by preferencesDataStore(name = TASK_PREFERENCES_NAME)

// 複数の同じ型のインスタンス（DataStore<Preferences>）を区別するための目印
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class TaskDataStore

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
    @TaskDataStore // このProvidesがTask用であることを示す
    fun provideTaskRepository(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.taskStore
    }

    @Provides
    @Singleton // アプリ内で常に同じインスタンスを使用する
    fun provideEcoActionRepository(
        @ApplicationContext context: Context
    ): EcoActionRepository {
        return DataStoreEcoActionRepository(context)
    }

    @Provides
    @Singleton
    fun provideCheckedStateRepository(
        @ApplicationContext context: Context
    ): com.example.ecodule.repository.CheckedStateRepository {
        return DataStoreCheckedStateRepository(context)
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
