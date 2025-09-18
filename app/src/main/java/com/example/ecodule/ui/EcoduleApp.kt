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
import java.time.LocalDate

@Preview(showBackground = true)
@Composable
fun EcoduleApp() {
    EcoduleAppContent()
}

@Composable
fun EcoduleAppContent(
    modifier: Modifier = Modifier
) {
    val selectedDestination = remember { mutableStateOf(EcoduleRoute.CALENDAR) }
    val taskViewModel = remember { TaskViewModel() }
    val editingEventId = remember { mutableStateOf<String?>(null) }


    // みやそう変更点
    val today = LocalDate.now()
    val todayMonth: Int = today.monthValue
    val todayDay: Int = today.dayOfMonth
    val todayEvents = taskViewModel.events.filter { it.day == todayDay && it.month == todayMonth }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (selectedDestination.value == EcoduleRoute.CALENDAR) {
            CalendarContentScreen(
                modifier = Modifier.weight(1f),
                selectedDestination = selectedDestination,
                events = taskViewModel.events,
                onEventClick = { eventId ->
                    editingEventId.value = eventId
                    selectedDestination.value = EcoduleRoute.TASKS
                }
            )
        } else if (selectedDestination.value == EcoduleRoute.TASKSLIST) {
            TaskListContent(
                modifier = Modifier.weight(1f),
                // みやそう変更点
                hasTasks = todayEvents.isNotEmpty()
            )
        } else if (selectedDestination.value == EcoduleRoute.STATISTICS) {
            StatisticsContent(modifier = Modifier.weight(1f))
        } else if (selectedDestination.value == EcoduleRoute.SETTINGS) {
            SettingsContent(modifier = Modifier.weight(1f))
        } else if (selectedDestination.value == EcoduleRoute.TASKS) {
            AddTaskContent(
                modifier = Modifier.weight(1f),
                selectedDestination = selectedDestination,
                taskViewModel = taskViewModel,
                editingEventId = editingEventId.value,
                onEditComplete = { editingEventId.value = null }
            )
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


object EcoduleRoute {
    const val CALENDAR = "Calendar"
    const val TASKSLIST = "TasksList"
    const val STATISTICS = "Statistics"
    const val SETTINGS = "Settings"
    const val TASKS = "Tasks"
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
        iconTextId = R.string.destination_tasksList
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
