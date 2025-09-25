package com.example.ecodule.ui.account.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodule.ui.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
/**
 * アプリ全体のユーザー情報を管理するViewModel
 */
class UserViewModel : ViewModel() {

    // ユーザー情報を保持するStateFlow。外部からは変更不可。
    // 初期値はnull（未ログイン状態）
    private val _user = MutableStateFlow<UserData?>(null)
    val user = _user.asStateFlow()

    /**
     * ログイン成功時などにユーザー情報を保存する
     * @param id ユーザーID
     * @param email メールアドレス
     */
    fun saveUser(id: String, email: String, accessToken: String) {
        Log.d("UserViewModel", "Saving user: id=$id, email=$email")

        _user.value = UserData(id = id, email = email, accessToken = accessToken)
    }

    /**
     * ログアウト時などにユーザー情報をクリアする
     */
    fun clearUser() {
        _user.value = null
    }

    // 便利なゲッターをプロパティとして定義することも可能
    /**
     * 現在のユーザーIDを取得する。未ログインの場合はnull。
     */
    val currentUserId: String?
        get() = _user.value?.id

    /**
     * 現在のユーザーEmailを取得する。未ログインの場合はnull。
     */
    val currentUserEmail: String?
        get() = _user.value?.email
}