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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.ecodule.R
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.screen.AddTaskContent
import com.example.ecodule.ui.CalendarContent.model.TaskViewModel
import com.example.ecodule.ui.CalendarContentui.CalendarContent.screen.CalendarContentScreen
import com.example.ecodule.ui.settings.SettingsContentScreen
import com.example.ecodule.ui.settings.account.SettingsAccountScreen
import com.example.ecodule.ui.settings.account.SettingsUserNameScreen
import com.example.ecodule.ui.settings.details.SettingsDetailsScreen
import com.example.ecodule.ui.settings.integration.SettingsGoogleIntegrationScreen
import com.example.ecodule.ui.settings.notifications.SettingNotificationsScreen
import com.example.ecodule.ui.statistics.StatisticsContent
import com.example.ecodule.ui.taskListContent.TaskListContent
import com.example.ecodule.ui.theme.BottomNavBackground
import com.example.ecodule.ui.theme.BottomNavSelectedBackground
import com.example.ecodule.ui.theme.BottomNavSelectedIcon
import com.example.ecodule.ui.theme.BottomNavUnselectedIcon
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun EcoduleAppNavigation(
    modifier: Modifier = Modifier,
    isGuestMode: Boolean = false,
    onLogout: () -> Unit = {}
) {
    val selectedDestination = remember { mutableStateOf(EcoduleRoute.CALENDAR) }
    val taskViewModel: TaskViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel() // HiltからViewModelを取得
    val authViewModel: EcoduleAuthViewModel = hiltViewModel()
    val editingEventId = remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf("User Name") }
    var birthDate by remember { mutableStateOf("2001/01/01")}

    // 設定（画面間で保持）
    var showWeekNumbers by rememberSaveable { mutableStateOf(false) }
    var selectedWeekStartLabel by rememberSaveable { mutableStateOf("日曜日") }
    var selectedTaskDuration by rememberSaveable { mutableStateOf("60 分") } // 追加

    fun toDayOfWeek(label: String): DayOfWeek = when (label) {
        "土曜日" -> DayOfWeek.SATURDAY
        "月曜日" -> DayOfWeek.MONDAY
        else -> DayOfWeek.SUNDAY
    }
    val weekStart: DayOfWeek = remember(selectedWeekStartLabel) { toDayOfWeek(selectedWeekStartLabel) }

    fun parseDurationMinutes(label: String): Int {
        // "60 分" -> 60
        return label.split(" ").firstOrNull()?.toIntOrNull() ?: 60
    }
    val defaultTaskDurationMinutes by remember(selectedTaskDuration) {
        mutableStateOf(parseDurationMinutes(selectedTaskDuration))
    }

    val todayEvents by taskViewModel.todayEvents.collectAsState()
    val today = LocalDate.now()
    

    val hideBottomBarRoutes = listOf(
        EcoduleRoute.SETTINGSDETAILS,
        EcoduleRoute.SETTINGSNOTIFICATIONS,
        EcoduleRoute.SETTINGSGOOGLEINTEGRATION
    )

    val events by taskViewModel.events.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when (selectedDestination.value) {
            EcoduleRoute.CALENDAR -> {
                CalendarContentScreen(
                    modifier = Modifier.weight(1f),
                    selectedDestination = selectedDestination,
                    onEventClick = { eventId ->
                        editingEventId.value = eventId
                        selectedDestination.value = EcoduleRoute.TASKS
                    },
                    userViewModel = userViewModel,
                    taskViewModel = taskViewModel,
                    showWeekNumbers = showWeekNumbers,
                    weekStart = weekStart
                )
            }
            EcoduleRoute.TASKS -> {
                AddTaskContent(
                    modifier = Modifier.weight(1f),
                    selectedDestination = selectedDestination,
                    taskViewModel = taskViewModel,
                    editingEventId = editingEventId.value,
                    onEditComplete = { editingEventId.value = null },
                    // 追加: 既定のタスク長（分）
                    defaultTaskDurationMinutes = defaultTaskDurationMinutes
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
                    userName = userName,
                    // 週設定
                    selectedWeekStart = selectedWeekStartLabel,
                    onSelectedWeekStartChange = { selectedWeekStartLabel = it },
                    showWeekNumbers = showWeekNumbers,
                    onShowWeekNumbersChange = { showWeekNumbers = it },
                    // 既定のタスク長（親で保持）
                    selectedTaskDuration = selectedTaskDuration,
                    onSelectedTaskDurationChange = { selectedTaskDuration = it },
                    onNavigateUserName = { selectedDestination.value = EcoduleRoute.SETTINGSACCOUNT },
                    onNavigateNotifications = { selectedDestination.value = EcoduleRoute.SETTINGSNOTIFICATIONS },
                    onNavigateGoogleCalendar = { selectedDestination.value = EcoduleRoute.SETTINGSGOOGLEINTEGRATION },
                    onNavigateDetail = { selectedDestination.value = EcoduleRoute.SETTINGSDETAILS }
                )
            }
            EcoduleRoute.SETTINGSDETAILS -> {
                SettingsDetailsScreen(
                    modifier = if (hideBottomBarRoutes.contains(selectedDestination.value)) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.weight(1f)
                    },
                    onBackToSettings = { selectedDestination.value = EcoduleRoute.SETTINGS },
                    onNavigateLicense = { },
                    onNavigateTerms = { }
                )
            }
            EcoduleRoute.SETTINGSNOTIFICATIONS -> {
                SettingNotificationsScreen(
                    modifier = if (hideBottomBarRoutes.contains(selectedDestination.value)) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.weight(1f)
                    },
                    onBackToSettings = { selectedDestination.value = EcoduleRoute.SETTINGS }
                )
            }
            EcoduleRoute.SETTINGSACCOUNT -> {
                SettingsAccountScreen(
                    userName = userName,
                    onBackToSettings = { selectedDestination.value = EcoduleRoute.SETTINGS },
                    onChangeUserName = { selectedDestination.value = EcoduleRoute.SETTINGSUSERNAME },
                    currentBirthDate = birthDate,
                    onBirthDateChanged = { newbirthDate ->
                        birthDate = newbirthDate
                    },
                    onLogout = {
                        if (!isGuestMode) {
                            authViewModel.onLogout()
                        }
                    },
                )
            }
            EcoduleRoute.SETTINGSUSERNAME -> {
                SettingsUserNameScreen(
                    currentUserName = userName,
                    onBackToAccount = { selectedDestination.value = EcoduleRoute.SETTINGSACCOUNT },
                    onUserNameChanged = { newName ->
                        userName = newName
                        selectedDestination.value = EcoduleRoute.SETTINGSACCOUNT
                    },
                )
            }
            EcoduleRoute.SETTINGSGOOGLEINTEGRATION -> {
                SettingsGoogleIntegrationScreen(
                    initialGoogleLinked = false,
                    initialGoogleUserName = "",
                    initialGoogleEmail = "",
                    initialCalendarLinked = false,
                    onGoogleAccountLink = { true; "Test User" to "testuser@gmail.com" },
                    onGoogleAccountUnlink = { },
                    onCalendarLink = { },
                    onCalendarUnlink = { },
                    onBackToSettings = { selectedDestination.value = EcoduleRoute.SETTINGS }
                )
            }
        }

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
                            selectedIconColor = BottomNavSelectedIcon,
                            unselectedIconColor = BottomNavUnselectedIcon,
                            indicatorColor = BottomNavSelectedBackground
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