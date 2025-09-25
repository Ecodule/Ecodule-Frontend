package com.example.ecodule.repository.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ecodule.repository.UserRepository
import kotlinx.coroutines.flow.first

val Context.userStore: DataStore<Preferences> by preferencesDataStore(
    "user"
)

val BODY_KEY = stringPreferencesKey("body")

class DataStoreUserRepository(
    private val context: Context
): UserRepository {
    override suspend fun save(body: String) {
        context.userStore.edit { pref ->
            pref[BODY_KEY] = body
        }
    }

    override suspend fun load(): String {
        return context.userStore.data.first()[BODY_KEY] ?: ""
    }
}