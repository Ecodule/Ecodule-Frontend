package com.example.ecodule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.ecodule.R
import com.example.ecodule.ui.CalendarContentui.CalendarContent.screen.CalendarContentScreen
import com.example.ecodule.ui.CalendarContent.model.TaskViewModel
import com.example.ecodule.ui.account.AccountCreateScreen
import com.example.ecodule.ui.account.AccountForgotPasswordScreen
import com.example.ecodule.ui.account.AccountSignInScreen
import com.example.ecodule.ui.settings.SettingsContentScreen
import com.example.ecodule.ui.settings.details.SettingsDetailsScreen
import com.example.ecodule.ui.settings.notifications.SettingNotificationsScreen
import com.example.ecodule.ui.theme.BottomNavBackground
import com.example.ecodule.ui.theme.BottomNavSelectedBackground
import com.example.ecodule.ui.theme.BottomNavSelectedIcon
import com.example.ecodule.ui.theme.BottomNavUnselectedIcon
import java.time.LocalDate

// アプリの状態を定義
enum class AppState {
    LOGIN,
    MAIN_APP,
    SIGNUP,
    FORGOT_PASSWORD
}

@Preview(showBackground = true)
@Composable
fun EcoduleApp() {
    EcoduleAppNavigation()
}

@Composable
fun EcoduleAppNavigation() {
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
            EcoduleAppContent(
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

@Composable
fun EcoduleAppContent(
    modifier: Modifier = Modifier,
    isGuestMode: Boolean = false,
    onLogout: () -> Unit = {}
) {
    val selectedDestination = remember { mutableStateOf(EcoduleRoute.CALENDAR) }
    val taskViewModel = remember { TaskViewModel() }
    val editingEventId = remember { mutableStateOf<String?>(null) }

    val today = LocalDate.now()
    val todayMonth: Int = today.monthValue
    val todayDay: Int = today.dayOfMonth
    val todayEvents = taskViewModel.events.filter { it.startDate.dayOfMonth == todayDay && it.startDate.monthValue == todayMonth }

    // ボトムナビゲーションバーを表示しない画面のリスト
    val hideBottomBarRoutes = listOf(
        EcoduleRoute.SETTINGSDETAILS,
        EcoduleRoute.SETTINGSNOTIFICATIONS
        // 将来的に他の詳細画面も追加可能
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // メインコンテンツ
        when (selectedDestination.value) {
            EcoduleRoute.CALENDAR -> {
                CalendarContentScreen(
                    modifier = Modifier.weight(1f),
                    selectedDestination = selectedDestination,
                    events = taskViewModel.events,
                    onEventClick = { eventId ->
                        editingEventId.value = eventId
                        selectedDestination.value = EcoduleRoute.TASKS
                    }
                )
            }
            EcoduleRoute.TASKS -> {
                AddTaskContent(
                    modifier = Modifier.weight(1f),
                    selectedDestination = selectedDestination,
                    taskViewModel = taskViewModel,
                    editingEventId = editingEventId.value,
                    onEditComplete = { editingEventId.value = null }
                )
            }
            EcoduleRoute.TASKSLIST -> {
                TaskListContent(
                    modifier = Modifier.weight(1f),
                    todayEvents = todayEvents
                )
            }
            EcoduleRoute.STATISTICS -> {
                StatisticsContent(modifier = Modifier.weight(1f))
            }
            EcoduleRoute.SETTINGS -> {
                SettingsContentScreen(
                    modifier = Modifier.weight(1f),
                    onNavigateUserName = { /* 画面遷移: ユーザー名 */ },
                    onNavigateTimeZone = { /* 画面遷移: タイムゾーン */ },
                    onNavigateNotifications = {
                        selectedDestination.value = EcoduleRoute.SETTINGSNOTIFICATIONS
                    },
                    onNavigateGoogleCalendar = { /* 画面遷移: Googleカレンダー連携 */ },
                    onNavigateDetail = {
                        selectedDestination.value = EcoduleRoute.SETTINGSDETAILS
                    }
                )
            }
            EcoduleRoute.SETTINGSDETAILS -> {
                SettingsDetailsScreen(
                    modifier = if (hideBottomBarRoutes.contains(selectedDestination.value)) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.weight(1f)
                    },
                    onBackToSettings = {
                        selectedDestination.value = EcoduleRoute.SETTINGS
                    },
                    onNavigateLicense = { /* ライセンス画面への遷移 */ },
                    onNavigateTerms = { /* 利用規約画面への遷移 */ }
                )
            }
            EcoduleRoute.SETTINGSNOTIFICATIONS -> {
                SettingNotificationsScreen(
                    modifier = if (hideBottomBarRoutes.contains(selectedDestination.value)) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.weight(1f)
                    },
                    onBackToSettings = {
                        selectedDestination.value = EcoduleRoute.SETTINGS
                    }
                )
            }
        }

        // ナビゲーションバー（特定の画面では非表示）
        if (!hideBottomBarRoutes.contains(selectedDestination.value)) {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = BottomNavBackground
            ) {
                TOP_LEVEL_DESTINATIONS.forEach { replyDestination ->
                    NavigationBarItem(
                        selected = selectedDestination.value == replyDestination.route,
                        onClick = {
                            selectedDestination.value = replyDestination.route
                            if (replyDestination.route != EcoduleRoute.TASKS) {
                                editingEventId.value = null
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = replyDestination.selectedIcon,
                                contentDescription = stringResource(id = replyDestination.iconTextId)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BottomNavSelectedIcon, // 選択時のアイコン色
                            unselectedIconColor = BottomNavUnselectedIcon, // 非選択時のアイコン色
                            indicatorColor = BottomNavSelectedBackground // 選択時の背景色（インジケーター）
                        )
                    )
                }
            }
        }
    }
}

object EcoduleRoute {
    const val CALENDAR = "Calendar"
    const val TASKS = "Tasks"
    const val STATISTICS = "Statistics"
    const val TASKSLIST = "TasksList"
    const val SETTINGS = "Settings"
    const val SETTINGSDETAILS = "SettingsDetails"
    const val SETTINGSNOTIFICATIONS = "SettingsNotifications" // 新しく追加

    // 将来的に他の詳細画面も追加可能
    // const val SETTINGSUSERNAME = "SettingsUserName"
    // const val SETTINGSTIMEZONE = "SettingsTimeZone"
}

data class EcoduleTopLevelDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int
)

val TOP_LEVEL_DESTINATIONS = listOf(
    EcoduleTopLevelDestination(
        route = EcoduleRoute.CALENDAR,
        selectedIcon = Icons.Default.CalendarMonth,
        unselectedIcon = Icons.Default.CalendarMonth,
        iconTextId = R.string.destination_calendar
    ),
    EcoduleTopLevelDestination(
        route = EcoduleRoute.TASKSLIST,
        selectedIcon = Icons.Default.Task,
        unselectedIcon = Icons.Default.Task,
        iconTextId = R.string.destination_tasks
    ),
    EcoduleTopLevelDestination(
        route = EcoduleRoute.STATISTICS,
        selectedIcon = Icons.Outlined.Analytics,
        unselectedIcon = Icons.Outlined.Analytics,
        iconTextId = R.string.destination_statistics
    ),
    EcoduleTopLevelDestination(
        route = EcoduleRoute.SETTINGS,
        selectedIcon = Icons.Default.Settings,
        unselectedIcon = Icons.Default.Settings,
        iconTextId = R.string.destination_settings
    )
)