package com.example.ecodule.ui.taskListContent.model

import androidx.compose.runtime.mutableStateOf
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecodule.repository.EcoAction
import com.example.ecodule.repository.EcoActionCategory
import com.example.ecodule.repository.EcoActionRepository
import com.example.ecodule.repository.UserRepository
import com.example.ecodule.repository.datastore.DataStoreTaskRepository
import com.example.ecodule.repository.datastore.TokenManager
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.model.TaskViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.String

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val ecoActionRepository: EcoActionRepository,
) : ViewModel() {
    // ここにタスクリストに関する状態管理やロジックを追加
    val isLoadingEcoAction = mutableStateOf(false)
    val isSendingAchievement = mutableStateOf(false)

    private val _checkedStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedStates: StateFlow<Map<String, Boolean>> = _checkedStates

    private val _expandedStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedStates: StateFlow<Map<String, Boolean>> = _expandedStates

    fun setChecked(key: String, checked: Boolean) {
        _checkedStates.value = _checkedStates.value.toMutableMap().apply {
            put(key, checked)
        }
    }

    fun toggleExpanded(key: String) {
        _expandedStates.value = _expandedStates.value.toMutableMap().apply {
            val current = this[key] ?: false
            put(key, !current)
        }
    }

    fun getCategorizeEcoActions(event: CalendarEvent): List<EcoAction> {
        val category = when (event.category) {
            "買い物" -> EcoActionCategory.SHOPPING
            "外出" -> EcoActionCategory.OUTING
            "ゴミ出し" -> EcoActionCategory.GARBAGE
            "通勤/通学" -> EcoActionCategory.COMMUTE
            else -> null
        }
        var ecoActions: List<EcoAction> = emptyList()

        if (category != null) {
            viewModelScope.launch {
                isLoadingEcoAction.value = true
                ecoActions = ecoActionRepository.getActionsForCategory(category)
                isLoadingEcoAction.value = false
            }
        }

        return ecoActions
    }
}