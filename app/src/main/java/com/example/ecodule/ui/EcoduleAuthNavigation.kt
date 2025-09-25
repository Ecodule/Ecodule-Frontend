package com.example.ecodule.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ecodule.ui.account.AccountCreateScreen
import com.example.ecodule.ui.account.AccountForgotPasswordScreen
import com.example.ecodule.ui.account.AccountSignInScreen

@Composable
fun EcoduleAuthNavigation(
    authViewModel: EcoduleAuthViewModel = hiltViewModel()
) {
    // ViewModelから認証状態とゲストモードの状態を監視
    val authState by authViewModel.authState.collectAsState()
    val isGuestMode by authViewModel.isGuestMode.collectAsState()

    // Composable内で画面の状態を管理する変数
    // (SIGNUPやFORGOT_PASSWORD画面への遷移のため)
    val screenState = remember { mutableStateOf(AuthScreenState.LOGIN) }

    when (authState) {
        AuthState.LOADING -> {
            // 読み込み中はインジケータを表示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        AuthState.LOGGED_OUT -> {
            // ログアウト状態の場合、ログイン関連の画面を表示
            when (screenState.value) {
                AuthScreenState.LOGIN -> AccountSignInScreen(
                    onLoginSuccess = { authViewModel.onLoginSuccess() },
                    onForgotPassword = { screenState.value = AuthScreenState.FORGOT_PASSWORD },
                    onSignUp = { screenState.value = AuthScreenState.SIGNUP },
                    onGoogleSignIn = { authViewModel.onLoginSuccess() },
                    onGuestMode = { authViewModel.onGuestMode() }
                )
                AuthScreenState.SIGNUP -> AccountCreateScreen(
                    onCreateSuccess = { authViewModel.onLoginSuccess() },
                    onBackToLogin = { screenState.value = AuthScreenState.LOGIN },
                    onGoogleCreate = { authViewModel.onLoginSuccess() }
                )
                AuthScreenState.FORGOT_PASSWORD -> AccountForgotPasswordScreen(
                    onBackToLogin = { screenState.value = AuthScreenState.LOGIN },
                    onPasswordResetSent = { screenState.value = AuthScreenState.LOGIN }
                )
            }
        }
        AuthState.LOGGED_IN -> {
            // ログイン済みの場合はメインアプリのナビゲーションを表示
            EcoduleAppNavigation(
                isGuestMode = isGuestMode,
                onLogout = { authViewModel.onLogout() }
            )
        }
    }
}

// ログアウト時の画面状態を定義
private enum class AuthScreenState {
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD
}