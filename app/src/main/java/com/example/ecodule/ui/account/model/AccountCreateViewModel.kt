package com.example.ecodule.ui.account.model

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodule.repository.UserRepository
import com.example.ecodule.repository.datastore.TokenManager
import com.example.ecodule.ui.account.api.AccountCreateApi
import com.example.ecodule.ui.account.api.AccountCreateResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountCreateViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository,
) : ViewModel() {
    // UIの状態を管理するState
    val accountCreateError = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)

    val accountCreateMessage = mutableStateOf("")

    // ログイン成功をUIに通知するためのイベントフロー
    private val _accountCreateSuccessEvent = MutableSharedFlow<Unit>()
    val accountCreateSuccessEvent = _accountCreateSuccessEvent.asSharedFlow()


    // ログイン処理を実行するメソッド
    fun accountCreate(email: String, password: String) {
        // viewModelScopeでコルーチンを起動
        viewModelScope.launch {
            isLoading.value = true // ローディング開始
            accountCreateError.value = null

            when (val result = AccountCreateApi.accountCreate(email, password)) {
                is AccountCreateResult.Success -> {
                    Log.d("AccountCreateViewModel", "Account created: ${result.email}, ${result.message}")
                    // 成功：メールアドレスにアカウント有効化メールを送信
                    _accountCreateSuccessEvent.emit(Unit)

                    accountCreateMessage.value = result.message
                }

                is AccountCreateResult.Error -> {
                    // 失敗：エラーメッセージを更新
                    Log.e("AccountCreateViewModel", "Account creation failed: ${result.message}")
                    accountCreateError.value = result.message
                }
            }
            isLoading.value = false // ローディング終了
        }
    }
}