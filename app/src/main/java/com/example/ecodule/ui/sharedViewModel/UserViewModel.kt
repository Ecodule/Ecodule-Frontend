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
import javax.inject.Inject

/**
 * アプリ全体のユーザー情報を管理するViewModel
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repo: UserRepository
) : ViewModel() {

    // ★ UserRepositoryのuser StateFlowをそのままUIに公開する
    val user = repo.user

    // 便利なゲッタープロパティ
    val currentUserId: String?
        get() = user.value?.id

    val currentUserEmail: String?
        get() = user.value?.email
}