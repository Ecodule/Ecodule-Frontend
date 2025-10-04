package com.example.ecodule.ui.taskListContent.model

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecodule.repository.EcoAction
import com.example.ecodule.repository.EcoActionCategory
import com.example.ecodule.repository.EcoActionRepository
import com.example.ecodule.repository.UserData
import com.example.ecodule.repository.UserRepository
import com.example.ecodule.repository.CheckedStateRepository
import com.example.ecodule.repository.datastore.DataStoreTaskRepository
import com.example.ecodule.repository.datastore.TokenManager
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.model.TaskViewModel
import com.example.ecodule.ui.account.api.LoginApi
import com.example.ecodule.ui.account.api.LoginResult
import com.example.ecodule.ui.taskListContent.api.UpdateAchievement
import com.example.ecodule.ui.taskListContent.api.UpdateAchievementResult
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
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository,
    private val checkedStateRepository: CheckedStateRepository, // 追加: チェック状態の永続化
) : ViewModel() {
    // ここにタスクリストに関する状態管理やロジックを追加
    val isLoadingEcoAction = mutableStateOf(false)

    private val _isSendingAchievement = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isSendingAchievement: StateFlow<Map<String, Boolean>> = _isSendingAchievement

    private val _sendingAchievementError = MutableStateFlow<Map<String, String?>>(emptyMap())
    val sendingAchievementError: StateFlow<Map<String, String?>> = _sendingAchievementError

    // 永続化されたチェック状態を DataStore から購読する
    private val user = userRepository.user
    private val userId = user.value?.id
    private val userEmail = user.value?.email

    // DataStore から読み出すため、ローカルのMap管理を廃止し、Repositoryのフローを公開
    val checkedStates: StateFlow<Map<String, Boolean>> =
        checkedStateRepository
            .observeCheckedStates(userId = userId ?: "")
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _expandedStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val expandedStates: StateFlow<Map<String, Boolean>> = _expandedStates

    fun setChecked(key: String, checked: Boolean, ecoAction: EcoAction) {
        // 送信中フラグ
        _isSendingAchievement.value = _isSendingAchievement.value.toMutableMap().apply {
            put(key, true)
        }
        _sendingAchievementError.value = _sendingAchievementError.value.toMutableMap().apply {
            put(key, null)
        }

        viewModelScope.launch {
            val accessToken = tokenManager.getAccessToken(userEmail = userEmail ?: "")

            val co2 = if (checked) ecoAction.co2Kg else -ecoAction.co2Kg
            val money = if (checked) ecoAction.savedYen else -ecoAction.savedYen

            when (val result = UpdateAchievement.updateAchievement(
                accessToken = accessToken ?: "",
                userId = userId ?: "",
                co2 = co2,
                money = money,
            )) {
                is UpdateAchievementResult.Success -> {
                    Log.d("TaskListViewModel", "UpdateAchievement successful: $result")
                    // 成功時に DataStore にも保存（状態は observeCheckedStates を通してUIに反映）
                    checkedStateRepository.setChecked(userId = userId ?: "", key = key, checked = checked)
                }
                is UpdateAchievementResult.Error -> {
                    Log.d("TaskListViewModel", "UpdateAchievement failed: $result")
                    _sendingAchievementError.value = _sendingAchievementError.value.toMutableMap().apply {
                        put(key, result.message)
                    }
                }
            }

            _isSendingAchievement.value = _isSendingAchievement.value.toMutableMap().apply {
                put(key, false)
            }
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