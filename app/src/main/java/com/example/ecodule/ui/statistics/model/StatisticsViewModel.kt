package com.example.ecodule.ui.statistics.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodule.repository.UserRepository
import com.example.ecodule.repository.datastore.TokenManager
import com.example.ecodule.ui.statistics.api.GetOverallStatisticsResult
import com.example.ecodule.ui.statistics.api.GetUserStatisticsResult
import com.example.ecodule.ui.statistics.api.StatisticsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository
) : ViewModel() {

    // ユーザー情報（TaskListViewModel と同じ取得スタイルに合わせる）
    private val user = userRepository.user
    private val userId = user.value?.id
    private val userEmail = user.value?.email

    // ユーザー統計の状態
    private val _isLoadingUserStats = MutableStateFlow(false)
    val isLoadingUserStats: StateFlow<Boolean> = _isLoadingUserStats

    private val _userStatsError = MutableStateFlow<String?>(null)
    val userStatsError: StateFlow<String?> = _userStatsError

    data class UserStatsUiState(
        val moneySaved: Double?,
        val co2Reduction: Double?
    )
    private val _userStats = MutableStateFlow<UserStatsUiState?>(null)
    val userStats: StateFlow<UserStatsUiState?> = _userStats

    // 全体統計の状態
    private val _isLoadingOverallStats = MutableStateFlow(false)
    val isLoadingOverallStats: StateFlow<Boolean> = _isLoadingOverallStats

    private val _overallStatsError = MutableStateFlow<String?>(null)
    val overallStatsError: StateFlow<String?> = _overallStatsError

    data class OverallStatsUiState(
        val totalMoneySaved: Double?,
        val totalCo2Reduction: Double?
    )
    private val _overallStats = MutableStateFlow<OverallStatsUiState?>(null)
    val overallStats: StateFlow<OverallStatsUiState?> = _overallStats

    /**
     * ユーザーの簡易統計を取得します（GET）。
     * レコードが存在しない場合、サーバ側で作成されて返る実装です。
     */
    fun fetchUserStatistics() {
        _isLoadingUserStats.value = true
        _userStatsError.value = null

        viewModelScope.launch {
            val accessToken = tokenManager.getAccessToken(userEmail = userEmail ?: "")
            if (accessToken.isNullOrBlank() || userId.isNullOrBlank()) {
                _isLoadingUserStats.value = false
                _userStatsError.value = "ユーザー情報またはトークンが取得できませんでした。"
                return@launch
            }

            when (val result = StatisticsApi.getUserSimpleStatistics(
                accessToken = accessToken,
                userId = userId
            )) {
                is GetUserStatisticsResult.Success -> {
                    _userStats.value = UserStatsUiState(
                        moneySaved = result.moneySaved,
                        co2Reduction = result.co2Reduction
                    )

                    Log.d("StatisticsViewModel", "fetchUserStatistics successful: $result")
                    _userStatsError.value = null
                }
                is GetUserStatisticsResult.Error -> {
                    Log.d("StatisticsViewModel", "fetchUserStatistics failed: ${result.message}")
                    _userStatsError.value = result.message
                }
            }
            _isLoadingUserStats.value = false
        }
    }

    /**
     * 全体統計を取得します（GET）。
     */
    fun fetchOverallStatistics() {
        _isLoadingOverallStats.value = true
        _overallStatsError.value = null

        viewModelScope.launch {
            val accessToken = tokenManager.getAccessToken(userEmail = userEmail ?: "")
            if (accessToken.isNullOrBlank()) {
                _isLoadingOverallStats.value = false
                _overallStatsError.value = "トークンが取得できませんでした。"
                return@launch
            }

            when (val result = StatisticsApi.getOverallStatistics(
                accessToken = accessToken
            )) {
                is GetOverallStatisticsResult.Success -> {
                    _overallStats.value = OverallStatsUiState(
                        totalMoneySaved = result.totalMoneySaved,
                        totalCo2Reduction = result.totalCo2Reduction
                    )
                    _overallStatsError.value = null
                }
                is GetOverallStatisticsResult.Error -> {
                    Log.d("StatisticsViewModel", "fetchOverallStatistics failed: ${result.message}")
                    _overallStatsError.value = result.message
                }
            }
            _isLoadingOverallStats.value = false
        }
    }
}