package com.example.ecodule.ui.taskListContent

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.UserViewModel
import com.example.ecodule.ui.taskListContent.component.TaskSectionWithTitleAndTime
import com.example.ecodule.ui.taskListContent.model.TaskListViewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val CategoryColorMap = mapOf(
    "ゴミ出し" to Color(0xFFB3E6FF),
    "通勤/通学" to Color(0xFFFFD2C5),
    "外出" to Color(0xFFE4EFCF),
    "買い物" to Color(0xFFC9E4D7)
)

fun darkenColor(color: Color, factor: Float = 0.6f): Color {
    val r = (color.red * factor).coerceIn(0f, 1f)
    val g = (color.green * factor).coerceIn(0f, 1f)
    val b = (color.blue * factor).coerceIn(0f, 1f)
    return Color(r, g, b, color.alpha)
}

@Composable
fun TaskListContent(
    modifier: Modifier = Modifier,
    todayEvents: List<CalendarEvent>,
    taskListViewModel: TaskListViewModel = hiltViewModel(),
) {
    if (todayEvents.isNotEmpty()) {
        val checkedStates by taskListViewModel.checkedStates.collectAsState()
        val expandedStates by taskListViewModel.expandedStates.collectAsState()

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(todayEvents.size) { index ->
                val event = todayEvents[index]
                taskListViewModel.getCategorizeEcoActions(event)
                val ecoActions = taskListViewModel.getCategorizeEcoActions(event)

//                Log.d("TaskListContent", "Rendering event: ${event.label} with actions: $ecoActions")
                val bgColor = CategoryColorMap[event.category] ?: Color(0xFFE0E0E0)

                val eventKey = "${event.label}-${event.startDate.hour}"

                TaskSectionWithTitleAndTime(
                    event = event,
                    items = ecoActions,
                    checkedStates = checkedStates,
                    expanded = expandedStates[eventKey] ?: false,
                    backgroundColor = bgColor,
                    onExpandToggle = {
                        taskListViewModel.toggleExpanded(eventKey)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (taskListViewModel.isLoadingEcoAction.value) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
            else {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "今日のタスクはありません",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
