package com.example.ecodule.repository.datastore

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ecodule.repository.CheckedStateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

// DataStoreTaskRepository と同様のスタイルで DataStore を定義
val Context.checkedStateStore: DataStore<Preferences> by preferencesDataStore("checked_state")
fun checkedStateKey(userId: String) = stringPreferencesKey("checked_${userId}")

@Serializable
data class CheckedStateEntry(
    val key: String,
    val checked: Boolean
)

@Singleton
class DataStoreCheckedStateRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : CheckedStateRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun observeCheckedStates(userId: String): StateFlow<Map<String, Boolean>> =
        context.checkedStateStore.data
            .map { pref ->
                val json = pref[checkedStateKey(userId)] ?: "[]"
                try {
                    val entries = Json.decodeFromString(
                        ListSerializer(CheckedStateEntry.serializer()),
                        json
                    )
                    entries.associate { it.key to it.checked }
                } catch (e: Exception) {
                    Log.e("CheckedStateRepo", "Failed to deserialize: ${e.message}")
                    emptyMap()
                }
            }
            .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override suspend fun setChecked(userId: String, key: String, checked: Boolean) {
        context.checkedStateStore.edit { pref ->
            val json = pref[checkedStateKey(userId)] ?: "[]"
            val current = try {
                Json.decodeFromString(
                    ListSerializer(CheckedStateEntry.serializer()),
                    json
                )
            } catch (e: Exception) {
                Log.e("CheckedStateRepo", "Failed to deserialize: ${e.message}")
                emptyList()
            }
            val updated = current
                .filterNot { it.key == key }
                .plus(CheckedStateEntry(key = key, checked = checked))
            pref[checkedStateKey(userId)] =
                Json.encodeToString(ListSerializer(CheckedStateEntry.serializer()), updated)
        }
    }

    override suspend fun setAll(userId: String, states: Map<String, Boolean>) {
        context.checkedStateStore.edit { pref ->
            val list = states.map { (k, v) -> CheckedStateEntry(k, v) }
            pref[checkedStateKey(userId)] =
                Json.encodeToString(ListSerializer(CheckedStateEntry.serializer()), list)
        }
    }

    override suspend fun clear(userId: String) {
        context.checkedStateStore.edit { pref ->
            pref.remove(checkedStateKey(userId))
        }
    }
}