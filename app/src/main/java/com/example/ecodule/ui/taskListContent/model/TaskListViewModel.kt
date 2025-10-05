package com.example.ecodule.ui.taskListContent.model

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodule.repository.EcoAction
import com.example.ecodule.repository.EcoActionCategory
import com.example.ecodule.repository.EcoActionRepository
import com.example.ecodule.repository.UserRepository
import com.example.ecodule.repository.CheckedStateRepository
import com.example.ecodule.repository.datastore.TokenManager
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.taskListContent.api.UpdateAchievement
import com.example.ecodule.ui.taskListContent.api.UpdateAchievementResult
import com.example.ecodule.ui.widget.AppWidget
import com.example.ecodule.ui.widget.reviewWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.String

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val application: Application, // 4. Applicationを注入
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
    private val userId = MutableStateFlow<String?>(null)
    private val userEmail = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            user.collect { userData ->
                userId.value = userData?.id
                userEmail.value = userData?.email
            }
        }
    }

    // DataStore から読み出すため、ローカルのMap管理を廃止し、Repositoryのフローを公開
    val checkedStates: StateFlow<Map<String, Boolean>> =
        checkedStateRepository
            .observeCheckedStates(userId = userId.value ?: "")
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
            val accessToken = tokenManager.getAccessToken(userEmail = userEmail.value ?: "")

            val co2 = if (checked) ecoAction.co2Kg else -ecoAction.co2Kg
            val money = if (checked) ecoAction.savedYen else -ecoAction.savedYen

            when (val result = UpdateAchievement.updateAchievement(
                accessToken = accessToken ?: "",
                userId = userId.value ?: "",
                co2 = co2,
                money = money,
            )) {
                is UpdateAchievementResult.Success -> {
                    Log.d("TaskListViewModel", "UpdateAchievement successful: $result")
                    // 成功時に DataStore にも保存（状態は observeCheckedStates を通してUIに反映）
                    checkedStateRepository.setChecked(userId = userId.value ?: "", key = key, checked = checked)

                    // 2. ウィジェットに更新を通知
                    updateAppWidget()
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

    /**
     * 6. AppWidgetを更新するための関数を追加
     */
    /**
     * AppWidgetを更新するための関数
     * Intentを直接ブロードキャストする方式に変更
     */
    private suspend fun updateAppWidget() {
        Log.d("TaskListViewModel", "Directly updating AppWidget state...")
        val manager = GlanceAppWidgetManager(application)
        val glanceIds = manager.getGlanceIds(AppWidget::class.java)

        if (glanceIds.isNotEmpty()) {
            glanceIds.forEach { glanceId ->
                reviewWidget(application, glanceId)
                // ★ 3. 状態更新後に、UIの再描画を要求
                AppWidget().update(application, glanceId)
            }
            Log.d("TaskListViewModel", "Direct update and recomposition triggered for IDs: $glanceIds")
        } else {
            Log.d("TaskListViewModel", "No AppWidgets found to update.")
        }
    }
}