package com.example.ecodule.ui.widget

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.action.ActionParameters

object PrefKeys {
    val currentEventJson = stringPreferencesKey("current_event_json")
    val currentEventId = stringPreferencesKey("current_event_id")
    val ecoActionsJson = stringPreferencesKey("eco_actions_json")
    val checkedJson = stringPreferencesKey("checked_entries_json")

    val ecoActionIdParam = ActionParameters.Key<String>("eco_action_id")
    val currentIndex = intPreferencesKey("current_event_index")
}

// MutablePreferences を直接クリアする拡張関数
fun MutablePreferences.removeAllForWidget() {
    remove(PrefKeys.currentEventJson)
    remove(PrefKeys.currentEventId)
    remove(PrefKeys.ecoActionsJson)
    remove(PrefKeys.checkedJson)
}