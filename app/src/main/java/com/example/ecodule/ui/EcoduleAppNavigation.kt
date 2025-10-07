package com.example.ecodule.ui

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.ecodule.R
import com.example.ecodule.ui.CalendarContent.model.CalendarMode
import com.example.ecodule.ui.CalendarContent.model.TaskViewModel
import com.example.ecodule.ui.CalendarContent.screen.AddTaskContent
import com.example.ecodule.ui.CalendarContentui.CalendarContent.screen.CalendarContentScreen
import com.example.ecodule.ui.animation.EcoduleAnimatedNavContainer
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun EcoduleAppNavigation(
    modifier: Modifier = Modifier,
    isGuestMode: Boolean = false,
    onLogout: () -> Unit = {}
) {
    val selectedDestination = remember { mutableStateOf(EcoduleRoute.CALENDAR) }
    val taskViewModel: TaskViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val authViewModel: EcoduleAuthViewModel = hiltViewModel()
    val editingEventId = remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf("User Name") }
    var birthDate by remember { mutableStateOf("2001/01/01") }

    var showWeekNumbers by rememberSaveable { mutableStateOf(false) }
    var selectedWeekStartLabel by rememberSaveable { mutableStateOf("日曜日") }
    var selectedTaskDuration by rememberSaveable { mutableStateOf("60 分") }

    // Helpers
    fun toDayOfWeek(label: String): DayOfWeek = when (label) {
        "土曜日" -> DayOfWeek.SATURDAY
        "月曜日" -> DayOfWeek.MONDAY
        else -> DayOfWeek.SUNDAY
    }
    val weekStart: DayOfWeek = remember(selectedWeekStartLabel) { toDayOfWeek(selectedWeekStartLabel) }
    fun parseDurationMinutes(label: String): Int =
        label.split(" ").firstOrNull()?.toIntOrNull() ?: 60
    val defaultTaskDurationMinutes by remember(selectedTaskDuration) {
        mutableStateOf(parseDurationMinutes(selectedTaskDuration))
    }

    val todayEvents by taskViewModel.todayEvents.collectAsState()

    // Routes that hide bottom bar
    val hideBottomBarRoutes = listOf(
        EcoduleRoute.SETTINGSDETAILS,
        EcoduleRoute.SETTINGSNOTIFICATIONS,
        EcoduleRoute.SETTINGSGOOGLEINTEGRATION,
        EcoduleRoute.SETTINGSACCOUNT,
        EcoduleRoute.SETTINGSUSERNAME,
        EcoduleRoute.TASKS
    )
    val showBottomBarTarget = !hideBottomBarRoutes.contains(selectedDestination.value)

    // カレンダー → タスク追加へ状態受け渡し用
    var pendingCalendarMode by remember { mutableStateOf<CalendarMode?>(null) }
    var pendingBaseDate by remember { mutableStateOf<LocalDate?>(null) }
    var pendingWeekStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var pendingThreeDayStart by remember { mutableStateOf<LocalDate?>(null) }
    var pendingYearMonth by remember { mutableStateOf<YearMonth?>(null) }

    // Bottom bar height (measured)
    val density = LocalDensity.current
    var measuredBarHeight by remember { mutableStateOf(56.dp) } // fallback 初期値
    // Transition for visibility + padding
    val animationDuration = 300
    val transition = updateTransition(targetState = showBottomBarTarget, label = "bottomBarVisibility")

    val bottomPadding: Dp by transition.animateDp(
        transitionSpec = { tween(animationDuration) },
        label = "bottomPadding"
    ) { visible ->
        if (visible) measuredBarHeight else 0.dp
    }

    // Alpha (fade)
    val barAlpha by transition.animateFloat(
        transitionSpec = { tween(animationDuration) },
        label = "barAlpha"
    ) { visible -> if (visible) 1f else 0f }

    // Optional slide (set slideEnabled=false でオフ)
    val slideEnabled = true
    val barTranslationY by transition.animateFloat(
        transitionSpec = { tween(animationDuration) },
        label = "barTranslationY"
    ) { visible ->
        if (!slideEnabled) 0f
        else {
            if (visible) 0f else with(density) { measuredBarHeight.toPx() * 0.3f } // 少し下へ
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Main content with animated bottom padding
        EcoduleAnimatedNavContainer(
            currentRoute = selectedDestination.value,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
        ) { route ->
            when (route) {
                EcoduleRoute.CALENDAR -> {
                    CalendarContentScreen(
                        modifier = Modifier.fillMaxSize(),
                        selectedDestination = selectedDestination,
                        onEventClick = { eventId ->
                            editingEventId.value = eventId
                            selectedDestination.value = EcoduleRoute.TASKS
                        },
                        userViewModel = userViewModel,
                        taskViewModel = taskViewModel,
                        showWeekNumbers = showWeekNumbers,
                        weekStart = weekStart,
                        onAddTaskRequest = { mode, base, weekStartDate, threeDayStart, ym ->
                            pendingCalendarMode = mode
                            pendingBaseDate = base
                            pendingWeekStartDate = weekStartDate
                            pendingThreeDayStart = threeDayStart
                            pendingYearMonth = ym
                            editingEventId.value = null
                            selectedDestination.value = EcoduleRoute.TASKS
                        }
                    )
                }
                EcoduleRoute.TASKS -> {
                    AddTaskContent(
                        modifier = Modifier.fillMaxSize(),
                        selectedDestination = selectedDestination,
                        taskViewModel = taskViewModel,
                        editingEventId = editingEventId.value,
                        onEditComplete = { editingEventId.value = null },
                        defaultTaskDurationMinutes = defaultTaskDurationMinutes,
                        calendarMode = pendingCalendarMode,
                        displayedBaseDate = pendingBaseDate,
                        weekStartDate = pendingWeekStartDate,
                        threeDayStartDate = pendingThreeDayStart,
                        displayedYearMonth = pendingYearMonth
                    )
                }
                EcoduleRoute.TASKSLIST -> {
                    TaskListContent(
                        modifier = Modifier.fillMaxSize(),
                        todayEvents = todayEvents
                    )
                }
                EcoduleRoute.STATISTICS -> {
                    StatisticsContent(modifier = Modifier.fillMaxSize())
                }
                EcoduleRoute.SETTINGS -> {
                    SettingsContentScreen(
                        modifier = Modifier.fillMaxSize(),
                        userName = userName,
                        selectedWeekStart = selectedWeekStartLabel,
                        onSelectedWeekStartChange = { selectedWeekStartLabel = it },
                        showWeekNumbers = showWeekNumbers,
                        onShowWeekNumbersChange = { showWeekNumbers = it },
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
                        modifier = Modifier.fillMaxSize(),
                        onBackToSettings = { selectedDestination.value = EcoduleRoute.SETTINGS },
                        onNavigateLicense = { },
                        onNavigateTerms = { }
                    )
                }
                EcoduleRoute.SETTINGSNOTIFICATIONS -> {
                    SettingNotificationsScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBackToSettings = { selectedDestination.value = EcoduleRoute.SETTINGS }
                    )
                }
                EcoduleRoute.SETTINGSACCOUNT -> {
                    SettingsAccountScreen(
                        userName = userName,
                        onBackToSettings = { selectedDestination.value = EcoduleRoute.SETTINGS },
                        onChangeUserName = { selectedDestination.value = EcoduleRoute.SETTINGSUSERNAME },
                        currentBirthDate = birthDate,
                        onBirthDateChanged = { newDate -> birthDate = newDate },
                        onLogout = {
                            if (!isGuestMode) authViewModel.onLogout()
                        }
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
        }

        // Bottom Navigation (always composed; fade / slide / height measured)
        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onGloballyPositioned { coords ->
                    val h = with(density) { coords.size.height.toDp() }
                    if (h > 0.dp) measuredBarHeight = h
                }
                .graphicsLayer {
                    alpha = barAlpha
                    translationY = barTranslationY
                },
            containerColor = BottomNavBackground
        ) {
            // クリック時: ルート切替
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
                    enabled = barAlpha > 0.4f, // フェードアウト中押下抑制 (任意)
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

// ルート・デスティネーション定義（変更なし）
object EcoduleRoute {
    const val CALENDAR = "Calendar"
    const val TASKS = "Tasks"
    const val STATISTICS = "Statistics"
    const val TASKSLIST = "TasksList"
    const val SETTINGS = "Settings"
    const val SETTINGSDETAILS = "SettingsDetails"
    const val SETTINGSNOTIFICATIONS = "SettingsNotifications"
    const val SETTINGSACCOUNT = "SettingsAccount"
    const val SETTINGSUSERNAME = "SettingsUserName"
    const val SETTINGSGOOGLEINTEGRATION = "SettingsGoogleIntegration"
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