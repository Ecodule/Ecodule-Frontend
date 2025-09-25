package com.example.ecodule.ui.account.model

import androidx.compose.runtime.mutableStateOf
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodule.ui.account.api.LoginApi
import com.example.ecodule.ui.util.TokenManager
import com.example.ecodule.ui.account.model.UserViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // TokenManagerをViewModel内で初期化
    private val tokenManager = TokenManager(application.applicationContext)

    // UIの状態を管理するState
    val loginError = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)

    // ログイン成功をUIに通知するためのイベントフロー
    private val _loginSuccessEvent = MutableSharedFlow<Unit>()
    val loginSuccessEvent = _loginSuccessEvent.asSharedFlow()

    // ログイン処理を実行するメソッド
    fun login(email: String, password: String) {
        // viewModelScopeでコルーチンを起動
        viewModelScope.launch {
            isLoading.value = true // ローディング開始
            loginError.value = null

            LoginApi.login(email, password) { success, message, id, accessToken, refreshToken, expiresIn ->
                if (success && accessToken != null && refreshToken != null && expiresIn != null && id != null) {
                    // 成功：トークンを保存
                    tokenManager.saveTokens(accessToken, refreshToken, expiresIn)
                    // ユーザー情報を保存
                    UserViewModel().saveUser(id = id, email = email, accessToken = accessToken) // ID
                    // 成功イベントを発行
                    viewModelScope.launch {
                        _loginSuccessEvent.emit(Unit)
                    }
                } else {
                    // 失敗：エラーメッセージを更新
                    loginError.value = message
                }
                isLoading.value = false // ローディング終了
            }
        }
    }
}