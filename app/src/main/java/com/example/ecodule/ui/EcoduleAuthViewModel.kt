package com.example.ecodule.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodule.repository.UserRepository
import com.example.ecodule.repository.datastore.TokenManager
import com.example.ecodule.ui.widget.AppWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EcoduleAuthViewModel @Inject constructor(
    private val application: Application, // 4. Applicationを注入
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState = _authState.asStateFlow()

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode = _isGuestMode.asStateFlow()

    init {
        // ViewModelが作成されたときに認証状態をチェックする
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            // まずUserRepositoryから保存されているユーザー情報を読み込む
            val userData = userRepository.user.value

            if (userData == null) {
                // ユーザー情報がなければログアウト状態
                _authState.value = AuthState.LOGGED_OUT
                return@launch
            }

            try {
                // ユーザー情報があれば、それを使ってアクセストークンを検証
                val token = tokenManager.getAccessToken(userData.email)

                if (token != null) {
                    // トークンが有効ならログイン状態
                    _authState.value = AuthState.LOGGED_IN
                } else {
                    // トークンが無効ならログアウト状態
                    _authState.value = AuthState.LOGGED_OUT
                }
            } catch (e: Exception) {
                // データのパース失敗などもログアウトとみなす
                _authState.value = AuthState.LOGGED_OUT
            }
        }
    }

    // ログイン成功時に呼び出される
    fun onLoginSuccess() {
        _isGuestMode.value = false
        _authState.value = AuthState.LOGGED_IN
    }

    // ゲストモードでのログイン
    fun onGuestMode() {
        _isGuestMode.value = true
        _authState.value = AuthState.LOGGED_IN
    }

    // ログアウト時に呼び出される
    fun onLogout() {
        viewModelScope.launch {
            // Logoutデバッグ
            Log.d("EcoduleAuthViewModel", "Logging out ${userRepository.user.value?.toString()} ${tokenManager.getAccessToken(userRepository.user.value?.email ?: "")}")
            // DataStoreとSharedPreferencesから情報を削除
            userRepository.clearUser()
            tokenManager.deleteTokens()
            _isGuestMode.value = false
            _authState.value = AuthState.LOGGED_OUT
        }
    }

    /**
     * 7. AppWidgetを更新するための関数を追加
     */
    private suspend fun updateAppWidget() {
        val manager = GlanceAppWidgetManager(application)
        val glanceIds = manager.getGlanceIds(AppWidget::class.java)
        glanceIds.forEach { glanceId ->
            AppWidget().update(application, glanceId)
        }
    }
}

// 認証状態を定義するEnum
enum class AuthState {
    LOADING,      // 確認中
    LOGGED_IN,    // ログイン済み
    LOGGED_OUT    // ログアウト済み
}