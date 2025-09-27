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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.ecodule.R
import com.example.ecodule.ui.CalendarContent.screen.AddTaskContent
import com.example.ecodule.ui.CalendarContent.model.TaskViewModel
import com.example.ecodule.ui.CalendarContentui.CalendarContent.screen.CalendarContentScreen
import com.example.ecodule.ui.settings.SettingsContentScreen
import com.example.ecodule.ui.settings.account.SettingsAccountScreen
import com.example.ecodule.ui.settings.account.SettingsUserNameScreen
import com.example.ecodule.ui.settings.details.SettingsDetailsScreen
import com.example.ecodule.ui.settings.notifications.SettingNotificationsScreen
import com.example.ecodule.ui.settings.integration.SettingsGoogleIntegrationScreen
import com.example.ecodule.ui.statistics.StatisticsContent
import com.example.ecodule.ui.taskListContent.TaskListContent
import com.example.ecodule.ui.theme.BottomNavBackground
import com.example.ecodule.ui.theme.BottomNavSelectedBackground
import com.example.ecodule.ui.theme.BottomNavSelectedIcon
import com.example.ecodule.ui.theme.BottomNavUnselectedIcon
import java.time.LocalDate

@Composable
fun EcoduleAppNavigation(
    modifier: Modifier = Modifier,
    isGuestMode: Boolean = false,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val selectedDestination = remember { mutableStateOf(EcoduleRoute.CALENDAR) }
    val taskViewModel = remember { TaskViewModel() }
    val userViewModel: UserViewModel = hiltViewModel() // HiltからViewModelを取得
    val editingEventId = remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf("User Name") }

    val today = LocalDate.now()
    val todayMonth: Int = today.monthValue
    val todayDay: Int = today.dayOfMonth
    val todayEvents = taskViewModel.events.filter { it.startDate.dayOfMonth == todayDay && it.startDate.monthValue == todayMonth }

    // ボトムナビゲーションバーを表示しない画面のリスト
    val hideBottomBarRoutes = listOf(
        EcoduleRoute.SETTINGSDETAILS,
        EcoduleRoute.SETTINGSNOTIFICATIONS,
        EcoduleRoute.SETTINGSGOOGLEINTEGRATION
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
                    },
                    userViewModel = userViewModel,
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
                    userName = userName,
                    modifier = Modifier.weight(1f),
                    onNavigateUserName = {
                        selectedDestination.value = EcoduleRoute.SETTINGSACCOUNT
                    },
                    onNavigateTimeZone = { /* 画面遷移: タイムゾーン */ },
                    onNavigateNotifications = {
                        selectedDestination.value = EcoduleRoute.SETTINGSNOTIFICATIONS
                    },
                    onNavigateGoogleCalendar = { 
                        selectedDestination.value = EcoduleRoute.SETTINGSGOOGLEINTEGRATION 
                    },
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
            EcoduleRoute.SETTINGSACCOUNT -> {
                SettingsAccountScreen(
                    // 適宜ユーザー情報等を渡す
                    userName = userName,
                    onBackToSettings = { selectedDestination.value = EcoduleRoute.SETTINGS },
                    onChangeUserName = { selectedDestination.value = EcoduleRoute.SETTINGSUSERNAME },
                    // その他変更画面遷移やイベント処理
                )
            }
            EcoduleRoute.SETTINGSUSERNAME -> {
                SettingsUserNameScreen(
                    currentUserName = userName,
                    onBackToAccount = { selectedDestination.value = EcoduleRoute.SETTINGSACCOUNT },
                    onUserNameChanged = { newName ->
                        userName = newName
                        selectedDestination.value = EcoduleRoute.SETTINGSACCOUNT
                    }
                )
            }
            EcoduleRoute.SETTINGSGOOGLEINTEGRATION -> {
                SettingsGoogleIntegrationScreen(
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
    const val SETTINGSDETAILS = "SettingsDetails" // 新しく追加
    const val SETTINGSNOTIFICATIONS = "SettingsNotifications"
    const val SETTINGSACCOUNT = "SettingsAccount"
    const val SETTINGSUSERNAME = "SettingsUserName"
    const val SETTINGSGOOGLEINTEGRATION = "SettingsGoogleIntegration"

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