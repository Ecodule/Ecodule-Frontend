package com.example.ecodule.ui.addtaskcontent.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ecodule.ui.CalendarContent.model.CalendarMode
import com.example.ecodule.ui.CalendarContent.model.TaskViewModel
import com.example.ecodule.ui.EcoduleRoute
import com.example.ecodule.ui.addtaskcontent.section.ActionButtons
import com.example.ecodule.ui.addtaskcontent.section.AllDaySwitch
import com.example.ecodule.ui.addtaskcontent.section.CategorySection
import com.example.ecodule.ui.addtaskcontent.section.DateTimeSection
import com.example.ecodule.ui.addtaskcontent.section.DeleteConfirmDialog
import com.example.ecodule.ui.addtaskcontent.section.ErrorMessage
import com.example.ecodule.ui.addtaskcontent.section.MemoSection
import com.example.ecodule.ui.addtaskcontent.section.NotificationSection
import com.example.ecodule.ui.addtaskcontent.section.RepeatSection
import com.example.ecodule.ui.addtaskcontent.section.TitleHeader
import com.example.ecodule.ui.addtaskcontent.section.TitleInput
import java.time.LocalDate
import java.time.YearMonth
import java.util.Date

@Composable
fun AddTaskContent(
    modifier: Modifier = Modifier,
    onSaveTask: (String, String, String, Boolean, Date?, Date?, String, String, Int) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onCancel: () -> Unit = {},
    selectedDestination: MutableState<String>,
    taskViewModel: TaskViewModel,
    editingEventId: String? = null,
    onEditComplete: () -> Unit = {},
    defaultTaskDurationMinutes: Int = 60,
    calendarMode: CalendarMode? = null,
    displayedBaseDate: LocalDate? = null,
    weekStartDate: LocalDate? = null,
    threeDayStartDate: LocalDate? = null,
    displayedYearMonth: YearMonth? = null
) {
    val state = rememberAddTaskState(
        taskViewModel = taskViewModel,
        editingEventId = editingEventId,
        calendarMode = calendarMode,
        displayedBaseDate = displayedBaseDate,
        weekStartDate = weekStartDate,
        threeDayStartDate = threeDayStartDate,
        displayedYearMonth = displayedYearMonth,
        defaultTaskDurationMinutes = defaultTaskDurationMinutes,
        onNavigateBackToCalendar = { selectedDestination.value = EcoduleRoute.CALENDAR },
        onEditComplete = onEditComplete
    )

    val ui = state.uiState

    val categories = remember {
        listOf(
            "ゴミ出し" to Color(0xFFB3E6FF),
            "通勤/通学" to Color(0xFFFFD2C5),
            "外出" to Color(0xFFE4EFCF),
            "買い物" to Color(0xFFC9E4D7)
        )
    }

    Log.d("AddTaskContent", "Categories = $categories")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleHeader(titleText = state.titleText, isEditing = state.isEditing) {
            state.showDeleteDialog(true)
        }
        TitleInput(
            value = ui.title,
            onValueChange = state::updateTitle
        )
        CategorySection(
            categories = categories,
            selectedCategory = ui.category,
            onCategorySelected = state::updateCategory
        )
        AllDaySwitch(
            allDay = ui.allDay,
            onChange = state::updateAllDay
        )
        DateTimeSection(
            allDay = ui.allDay,
            startDate = state.displayStartDate,
            startTime = state.displayStartTime,
            endDate = state.displayEndDate,
            endTime = state.displayEndTime,
            onStartDateChange = state::updateStartDate,
            onStartTimeChange = state::updateStartTime,
            onEndDateChange = state::updateEndDate,
            onEndTimeChange = state::updateEndTime
        )
        if (state.showEndBeforeStartError) {
            ErrorMessage("終了日は開始日以降に設定してください")
        }
        RepeatSection(
            repeatOption = ui.repeatOption,
            onRepeatOptionChange = state::updateRepeatOption
        )
        NotificationSection(
            notificationMinutes = ui.notificationMinutes,
            onNotificationChange = state::updateNotificationMinutes
        )
        MemoSection(
            memo = ui.memo,
            onMemoChange = state::updateMemo
        )
        ActionButtons(
            onCancel = state::onCancel,
            onSave = state::onSave,
            canSave = state.canSave,
            saveLabel = state.buttonText
        )
    }

    if (ui.showDeleteDialog) {
        DeleteConfirmDialog(
            onDismiss = { state.showDeleteDialog(false) },
            onConfirmDelete = state::onDeleteConfirmed
        )
    }
}

