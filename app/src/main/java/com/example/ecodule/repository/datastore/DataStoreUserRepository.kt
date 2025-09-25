package com.example.ecodule.repository.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ecodule.repository.UserRepository
import com.example.ecodule.ui.sharedViewModel.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject

val Context.userStore: DataStore<Preferences> by preferencesDataStore(
    "user"
)

val BODY_KEY = stringPreferencesKey("body")

class DataStoreUserRepository @Inject constructor(
    private val context: Context
): UserRepository {

    // アプリケーションスコープのコルーチン
    private val scope = CoroutineScope(Dispatchers.IO)

    // ★ DataStoreのFlowを監視し、StateFlowに変換して公開
    override val user: StateFlow<UserData?> = context.userStore.data
        .map { pref ->
            val userJson = pref[BODY_KEY] ?: ""
            if (userJson.isNotEmpty()) {
                try {
                    Json.decodeFromString<UserData>(userJson)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override suspend fun saveUser(id: String, email: String) {
        val newUser = UserData(id = id, email = email)
        val userJson = Json.encodeToString(UserData.serializer(), newUser)
        context.userStore.edit { pref ->
            pref[BODY_KEY] = userJson
        }
    }

    override suspend fun clearUser() {
        context.userStore.edit { pref ->
            pref.remove(BODY_KEY)
        }
    }
}