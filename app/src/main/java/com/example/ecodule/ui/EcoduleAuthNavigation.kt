package com.example.ecodule.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.ecodule.ui.account.AccountCreateScreen
import com.example.ecodule.ui.account.AccountForgotPasswordScreen
import com.example.ecodule.ui.account.AccountSignInScreen


// アプリの状態を定義
enum class AppState {
    LOGIN,
    MAIN_APP,
    SIGNUP,
    FORGOT_PASSWORD
}

@Composable
fun EcoduleAuthNavigation() {
    // アプリ全体の状態管理
    val appState = remember { mutableStateOf(AppState.LOGIN) }
    val isGuestMode = remember { mutableStateOf(false) }

    when (appState.value) {
        AppState.LOGIN -> {
            AccountSignInScreen(
                onLoginSuccess = {
                    // ログイン成功時にメインアプリへ
                    isGuestMode.value = false
                    appState.value = AppState.MAIN_APP
                },
                onForgotPassword = {
                    // パスワード忘れ画面へ
                    appState.value = AppState.FORGOT_PASSWORD
                },
                onSignUp = {
                    // サインアップ画面へ
                    appState.value = AppState.SIGNUP
                },
                onGoogleSignIn = {
                    // Googleサインイン成功時にメインアプリへ
                    isGuestMode.value = false
                    appState.value = AppState.MAIN_APP
                },
                onGuestMode = {
                    // ゲストモードでメインアプリへ
                    isGuestMode.value = true
                    appState.value = AppState.MAIN_APP
                }
            )
        }
        AppState.MAIN_APP -> {
            EcoduleAppNavigation(
                isGuestMode = isGuestMode.value,
                onLogout = {
                    // ログアウト時にログイン画面へ戻る
                    isGuestMode.value = false
                    appState.value = AppState.LOGIN
                }
            )
        }
        AppState.SIGNUP -> {
            // アカウント作成画面
            AccountCreateScreen(
                onCreateSuccess = {
                    // アカウント作成成功時にメインアプリへ
                    isGuestMode.value = false
                    appState.value = AppState.MAIN_APP
                },
                onBackToLogin = {
                    // ログイン画面へ戻る
                    appState.value = AppState.LOGIN
                },
                onGoogleCreate = {
                    // Googleアカウント作成成功時にメインアプリへ
                    isGuestMode.value = false
                    appState.value = AppState.MAIN_APP
                }
            )
        }
        AppState.FORGOT_PASSWORD -> {
            // パスワード忘れ画面
            AccountForgotPasswordScreen(
                onBackToLogin = {
                    // ログイン画面へ戻る
                    appState.value = AppState.LOGIN
                },
                onPasswordResetSent = {
                    // パスワードリセット送信後ログイン画面へ
                    appState.value = AppState.LOGIN
                }
            )
        }
    }
}
