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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.ecodule.R
import com.example.ecodule.ui.CalendarContentui.CalendarContent.screen.CalendarContentScreen
import com.example.ecodule.ui.CalendarContent.model.TaskViewModel
import com.example.ecodule.ui.account.AccountSignInScreen
import com.example.ecodule.ui.settings.SettingsContentScreen

import java.time.LocalDate

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
    val appState = remember { mutableStateOf(AppState.LOGIN) }
    val isGuestMode = remember { mutableStateOf(false) }

    when (appState.value) {
        AppState.LOGIN -> {
            AccountSignInScreen(
                onLoginSuccess = {
                    isGuestMode.value = false
                    appState.value = AppState.MAIN_APP
                },
                onForgotPassword = {
                    appState.value = AppState.FORGOT_PASSWORD
                },
                onSignUp = {
                    appState.value = AppState.SIGNUP
                },
                onGoogleSignIn = {
                    isGuestMode.value = false
                    appState.value = AppState.MAIN_APP
                },
                onGuestMode = {
                    isGuestMode.value = true
                    appState.value = AppState.MAIN_APP
                }
            )
        }
        AppState.MAIN_APP -> {
            EcoduleAppContent(
                isGuestMode = isGuestMode.value,
                onLogout = {
                    isGuestMode.value = false
                    appState.value = AppState.LOGIN
                }
            )
        }
        AppState.SIGNUP -> {
            AccountSignUpScreen(
                onSignUpSuccess = {
                    isGuestMode.value = false
                    appState.value = AppState.MAIN_APP
                },
                onBackToLogin = {
                    appState.value = AppState.LOGIN
                }
            )
        }
        AppState.FORGOT_PASSWORD -> {
            ForgotPasswordScreen(
                onBackToLogin = {
                    appState.value = AppState.LOGIN
                },
                onPasswordResetSent = {
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
    val todayEvents = taskViewModel.events.filter { it.day == todayDay && it.month == todayMonth }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
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
                    onNavigateNotifications = { /* 画面遷移: 通知 */ },
                    onNavigateGoogleCalendar = { /* 画面遷移: Googleカレンダー連携 */ },
                    onNavigateDetail = { /* 画面遷移: 詳細 */ }
                )
            }
        }


        NavigationBar(modifier = Modifier.fillMaxWidth()) {
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
                    }
                )
            }
        }
    }
}

@Composable
fun AccountSignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // サインアップ画面の実装
    }
}

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    onPasswordResetSent: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // パスワード忘れ画面の実装
    }
}

object EcoduleRoute {
    const val CALENDAR = "Calendar"
    const val TASKS = "Tasks"
    const val STATISTICS = "Statistics"
    const val SETTINGS = "Settings"
    const val TASKSLIST = "TasksList"
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