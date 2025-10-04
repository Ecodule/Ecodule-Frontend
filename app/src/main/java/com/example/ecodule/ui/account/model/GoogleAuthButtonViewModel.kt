package com.example.ecodule.ui.account.model

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodule.repository.UserRepository
import com.example.ecodule.repository.datastore.TokenManager
import com.example.ecodule.ui.account.api.GoogleLoginApi
import com.example.ecodule.ui.account.api.GoogleLoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoogleAuthButtonViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository,
) : ViewModel() {
    // UIの状態を管理するState
    val googleLoginError = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)

    // ログイン成功をUIに通知するためのイベントフロー
    private val _googleLoginSuccessEvent = MutableSharedFlow<Unit>()
    val googleLoginSuccessEvent = _googleLoginSuccessEvent.asSharedFlow()

    // ログイン処理を実行するメソッド
    fun googleLogin(token: String) {
        // viewModelScopeでコルーチンを起動
        viewModelScope.launch {
            isLoading.value = true // ローディング開始
            googleLoginError.value = null

            when (val result = GoogleLoginApi.login(token)) {
                is GoogleLoginResult.Success -> {

                    // 成功：トークンとユーザー情報を保存
                    tokenManager.saveTokens(result.accessToken, result.refreshToken, result.expiresIn)
                    // ★ これでsuspend関数をコルーチン内から安全に呼び出せる
                    userRepository.saveUser(id = result.id, email = result.email)

                    _googleLoginSuccessEvent.emit(Unit)
                }
                is GoogleLoginResult.Error -> {
                    Log.d("LoginViewModel", "Login failed: ${result}")
                    // 失敗：エラーメッセージを更新
                    googleLoginError.value = result.message
                }
            }
            isLoading.value = false // ローディング終了
        }
    }
}

