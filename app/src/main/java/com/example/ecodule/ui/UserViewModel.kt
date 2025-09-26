package com.example.ecodule.ui

import androidx.lifecycle.ViewModel
import com.example.ecodule.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * アプリ全体のユーザー情報を管理するViewModel
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepo: UserRepository,
) : ViewModel() {

    // ★ UserRepositoryのuser StateFlowをそのままUIに公開する
    val user = userRepo.user

    // 便利なゲッタープロパティ
    val currentUserId: String?
        get() = user.value?.id

    val currentUserEmail: String?
        get() = user.value?.email
}