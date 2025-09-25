package com.example.ecodule.ui.sharedViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodule.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * アプリ全体のユーザー情報を管理するViewModel
 */
@HiltViewModel
class UserViewModel(
    private val repo: UserRepository
) : ViewModel() {
    // DataStoreUserRepositoryをインスタンス化
    private val _user = MutableStateFlow<UserData?>(null)
    val user = _user.asStateFlow()

    init {
        // ViewModelが作成されたときに、DataStoreからユーザー情報を読み込む
        loadUserFromRepository()
    }

    /**
     * DataStoreからユーザー情報を非同期で読み込む
     */
    private fun loadUserFromRepository() {
        viewModelScope.launch {
            val userJson = repo.load()
            if (userJson.isNotEmpty()) {
                try {
                    // JSON文字列をUserDataオブジェクトに変換
                    _user.value = Json.decodeFromString<UserData>(userJson)
                    Log.d("UserViewModel", "User loaded from DataStore: ${user.value}")
                } catch (e: Exception) {
                    Log.e("UserViewModel", "Failed to parse user data from DataStore", e)
                    // パースに失敗した場合、保存されているデータをクリアする
                    clearUser()
                }
            }
        }
    }

    /**
     * ログイン成功時などにユーザー情報をStateFlowにセットし、DataStoreに保存する
     * @param id ユーザーID
     * @param email メールアドレス
     */
    fun saveUser(id: String, email: String) {
        viewModelScope.launch {
            val newUser = UserData(id = id, email = email)
            // StateFlowを更新
            _user.value = newUser

            try {
                // UserDataオブジェクトをJSON文字列に変換
                val userJson = Json.encodeToString(newUser)
                // DataStoreに保存
                repo.save(userJson)
                Log.d("UserViewModel", "User saved to DataStore: $userJson")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Failed to save user data to DataStore", e)
            }
        }
    }

    /**
     * ログアウト時などにユーザー情報をクリアし、DataStoreからも削除する
     */
    fun clearUser() {
        viewModelScope.launch {
            // StateFlowをクリア
            _user.value = null
            // DataStoreのデータを空文字列で上書きしてクリア
            repo.save("")
            Log.d("UserViewModel", "User cleared from DataStore.")
        }
    }

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