package com.example.ecodule.ui

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ecodule.ui.account.AccountCreateScreen
import com.example.ecodule.ui.account.AccountForgotPasswordScreen
import com.example.ecodule.ui.account.AccountSignInScreen
import com.example.ecodule.ui.account.AuthTermsScreen
import com.example.ecodule.ui.animation.EcoduleAnimatedNavContainer

@Composable
fun EcoduleAuthNavigation(
    authViewModel: EcoduleAuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    SideEffect {
        val window = (context as? Activity)?.window
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            val insetsController = WindowCompat.getInsetsController(it, view)
            insetsController?.isAppearanceLightStatusBars = true
        }
    }

    val authState by authViewModel.authState.collectAsState()
    val isGuestMode by authViewModel.isGuestMode.collectAsState()

    // ログアウト状態内での画面遷移管理
    val screenState = remember { mutableStateOf(AuthScreenState.LOGIN) }

    // 同意状態をナビゲーション側にホイスト
    val termsAcceptedState = remember { mutableStateOf(false) }

    // 大枠 (AuthState) の切り替えはシンプルにフェード
    AnimatedContent(
        targetState = authState,
        transitionSpec = {
            fadeIn(tween(250)) togetherWith fadeOut(tween(200))
        },
        label = "AuthStateAnimatedContent",
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) { state ->
        when (state) {
            AuthState.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            AuthState.LOGGED_OUT -> {
                // ここで LOGIN / SIGNUP / FORGOT / TERMS をアニメーション遷移
                EcoduleAnimatedNavContainer(
                    currentRoute = screenState.value.name
                ) { routeKey ->
                    val currentScreen = AuthScreenState.valueOf(routeKey)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        when (currentScreen) {
                            AuthScreenState.LOGIN -> AccountSignInScreen(
                                onLoginSuccess = { authViewModel.onLoginSuccess() },
                                onForgotPassword = { screenState.value = AuthScreenState.FORGOT_PASSWORD },
                                onSignUp = { screenState.value = AuthScreenState.SIGNUP },
                                onGuestMode = { authViewModel.onGuestMode() }
                            )
                            AuthScreenState.SIGNUP -> AccountCreateScreen(
                                onBackToLogin = {
                                    // 戻るときに利用規約チェックを必ずOFFにする
                                    termsAcceptedState.value = false
                                    screenState.value = AuthScreenState.LOGIN
                                },
                                onLoginSuccess = { authViewModel.onLoginSuccess() },
                                onOpenTerms = { screenState.value = AuthScreenState.TERMS },
                                termsAccepted = termsAcceptedState.value,
                                onTermsAcceptedChange = { termsAcceptedState.value = it },
                            )
                            AuthScreenState.FORGOT_PASSWORD -> AccountForgotPasswordScreen(
                                onBackToLogin = { screenState.value = AuthScreenState.LOGIN },
                                onPasswordResetSent = { screenState.value = AuthScreenState.LOGIN }
                            )
                            AuthScreenState.TERMS -> AuthTermsScreen(
                                onBack = { screenState.value = AuthScreenState.SIGNUP },
                                onAgreeAndBack = {
                                    // 「同意して戻る」でチェックONにしてサインアップ画面へ戻る
                                    termsAcceptedState.value = true
                                    screenState.value = AuthScreenState.SIGNUP
                                }
                            )
                        }
                    }
                }
            }
            AuthState.LOGGED_IN -> {
                EcoduleAppNavigation(
                    isGuestMode = isGuestMode,
                    onLogout = { authViewModel.onLogout() }
                )
            }
        }
    }
}

// ログアウト時の画面状態
private enum class AuthScreenState {
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD,
    TERMS
}