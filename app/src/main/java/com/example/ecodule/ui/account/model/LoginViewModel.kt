package com.example.ecodule.ui.account.model

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodule.repository.UserRepository
import com.example.ecodule.ui.account.api.LoginApi
import com.example.ecodule.ui.account.api.LoginResult
import com.example.ecodule.ui.UserViewModel
import com.example.ecodule.repository.datastore.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository,
) : ViewModel() {
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

            when (val result = LoginApi.login(email, password)) {
                is LoginResult.Success -> {

                    // 成功：トークンとユーザー情報を保存
                    tokenManager.saveTokens(result.accessToken, result.refreshToken, result.expiresIn)
                    // ★ これでsuspend関数をコルーチン内から安全に呼び出せる
                    userRepository.saveUser(id = result.id, email = email)

                    _loginSuccessEvent.emit(Unit)
                }
                is LoginResult.Error -> {
                    Log.d("LoginViewModel", "Login failed: ${result}")
                    // 失敗：エラーメッセージを更新
                    loginError.value = result.message
                }
            }
            isLoading.value = false // ローディング終了
        }
    }

    fun googleLogin() {

    }
}