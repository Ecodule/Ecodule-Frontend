package com.example.ecodule.ui.addtaskcontent.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ecodule.ui.CalendarContent.model.CalendarMode
import com.example.ecodule.ui.CalendarContent.model.TaskViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

/********************
 * UI状態ホルダー
 ********************/

data class AddTaskUiState(
    val title: String = "",
    val category: String = "ゴミ出し",
    val description: String = "",
    val allDay: Boolean = false,
    val startDateText: String = "",
    val startTimeText: String = "",
    val endDateText: String = "",
    val endTimeText: String = "",
    val repeatOption: String = "しない",
    val notificationMinutes: Int = 10,
    val memo: String = "",
    val showDeleteDialog: Boolean = false,
    val isEditing: Boolean = false,
    val editingEventId: String? = null
)

/**
 * 状態と保存/削除ロジック
 */
class AddTaskState(
    initial: AddTaskUiState,
    private val taskViewModel: TaskViewModel,
    private val editingEventId: String?,
    private val calendarMode: CalendarMode?,
    private val displayedBaseDate: LocalDate?,
    private val weekStartDate: LocalDate?,
    private val threeDayStartDate: LocalDate?,
    private val displayedYearMonth: YearMonth?,
    private val defaultTaskDurationMinutes: Int,
    private val today: LocalDate,
    private val onNavigateBackToCalendar: () -> Unit,
    private val onEditComplete: () -> Unit
) {
    var uiState by mutableStateOf(initial)
        private set

    private val sdfDateTime = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    private val sdfDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    private val fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    private val todayString = today.format(fmt)
    private val snappedStartTime = snapNowToNearest30()

    init {
        loadIfEditingOrSetInitialDate()
    }

    private fun loadIfEditingOrSetInitialDate() {
        if (editingEventId != null) {
            val event = taskViewModel.getEventById(editingEventId)
            event?.let { e ->
                val (sDate, sTime) = convertDateToStrings(e.startDate)
                val (eDate, eTime) = convertDateToStrings(e.endDate)
                uiState = uiState.copy(
                    title = e.label,
                    category = e.category,
                    allDay = e.allDay,
                    startDateText = sDate,
                    startTimeText = sTime,
                    endDateText = eDate,
                    endTimeText = eTime,
                    repeatOption = e.repeatOption,
                    memo = e.memo,
                    notificationMinutes = e.notificationMinutes
                )
            }
        } else {
            if (uiState.startDateText.isBlank()) {
                val initDate = determineInitialDate(
                    mode = calendarMode,
                    today = today,
                    baseDate = displayedBaseDate,
                    weekStart = weekStartDate,
                    threeDayStart = threeDayStartDate,
                    shownYearMonth = displayedYearMonth
                )
                uiState = uiState.copy(startDateText = initDate.format(fmt))
            }
        }
    }

    private fun convertDateToStrings(dateObj: Any?): Pair<String, String> {
        if (dateObj == null) return "" to ""
        val cal = Calendar.getInstance()
        when (dateObj) {
            is LocalDateTime -> {
                cal.time = Date.from(dateObj.atZone(ZoneId.systemDefault()).toInstant())
            }
            is Date -> cal.time = dateObj
        }
        val dateStr = "%04d/%02d/%02d".format(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
        )
        val timeStr = "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        return dateStr to timeStr
    }

    /*************** 入力更新 ***************/
    fun updateTitle(v: String) = mutate { copy(title = v) }
    fun updateCategory(v: String) = mutate { copy(category = v) }
    fun updateAllDay(v: Boolean) = mutate { copy(allDay = v) }
    fun updateStartDate(v: String) = mutate { copy(startDateText = v) }
    fun updateStartTime(v: String) = mutate { copy(startTimeText = v) }
    fun updateEndDate(v: String) = mutate { copy(endDateText = v) }
    fun updateEndTime(v: String) = mutate { copy(endTimeText = v) }
    fun updateRepeatOption(v: String) = mutate { copy(repeatOption = v) }
    fun updateNotificationMinutes(v: Int) = mutate { copy(notificationMinutes = v) }
    fun updateMemo(v: String) = mutate { copy(memo = v) }
    fun showDeleteDialog(show: Boolean) = mutate { copy(showDeleteDialog = show) }

    /*************** 派生値（内部計算用） ***************/
    private val actualStartDate: String
        get() = uiState.startDateText.ifBlank { todayString }

    private val actualStartTime: String
        get() = uiState.startTimeText.ifBlank { snappedStartTime }

    private val derivedEnd: Pair<String, String>
        get() = computeEndDateAndTime(actualStartDate, actualStartTime, defaultTaskDurationMinutes)

    private val actualEndDate: String
        get() = uiState.endDateText.ifBlank { derivedEnd.first }

    private val actualEndTime: String
        get() = uiState.endTimeText.ifBlank { derivedEnd.second }

    // UI 表示用（空欄時もスナップ・派生値を見せる）
    val displayStartDate: String get() = actualStartDate
    val displayStartTime: String get() = actualStartTime
    val displayEndDate: String get() = actualEndDate
    val displayEndTime: String get() = actualEndTime

    val startDate: Date? get() = parseDateTime(actualStartDate, actualStartTime, uiState.allDay)
    val endDate: Date? get() = parseDateTime(actualEndDate, actualEndTime, uiState.allDay)

    val isEditing: Boolean get() = editingEventId != null

    val canSave: Boolean
        get() = uiState.title.isNotBlank()
                && actualStartDate.isNotBlank()
                && actualEndDate.isNotBlank()
                && (uiState.allDay || (actualStartTime.isNotBlank() && actualEndTime.isNotBlank()))
                && startDate != null
                && endDate != null
                && !(endDate?.before(startDate) ?: true)

    val showEndBeforeStartError: Boolean
        get() = (startDate != null && endDate != null && endDate!!.before(startDate))

    val buttonText: String
        get() = if (isEditing) "更新" else "追加"

    val titleText: String
        get() = if (isEditing) "タスクを編集" else "タスクを追加"

    /*************** アクション ***************/
    fun onCancel() {
        onEditComplete()
        onNavigateBackToCalendar()
    }

    fun onSave() {
        if (!canSave) return
        val sDate = startDate ?: return
        val eDate = endDate ?: return

        if (isEditing && editingEventId != null) {
            val event = taskViewModel.getEventById(editingEventId)
            if (event?.repeatGroupId != null) {
                taskViewModel.updateEventsByRepeatGroup(
                    event.repeatGroupId,
                    uiState.title, uiState.category, uiState.description, uiState.allDay,
                    sDate, eDate, uiState.repeatOption, uiState.memo, uiState.notificationMinutes
                )
            } else {
                taskViewModel.updateEvent(
                    editingEventId,
                    uiState.title, uiState.category, uiState.description, uiState.allDay,
                    sDate, eDate, uiState.repeatOption, uiState.memo, uiState.notificationMinutes
                )
            }
        } else {
            taskViewModel.addEvent(
                uiState.title,
                uiState.category,
                uiState.description,
                uiState.allDay,
                sDate,
                eDate,
                uiState.repeatOption,
                uiState.memo,
                uiState.notificationMinutes,
                repeatGroupId = null
            )
        }
        onEditComplete()
        onNavigateBackToCalendar()
    }

    fun onDeleteConfirmed() {
        editingEventId?.let { taskViewModel.deleteEvent(it) }
        showDeleteDialog(false)
        onEditComplete()
        onNavigateBackToCalendar()
    }

    /*************** プライベート ***************/
    private fun parseDateTime(date: String, time: String, allDay: Boolean): Date? = try {
        if (allDay) sdfDate.parse(date)
        else if (date.isNotBlank() && time.isNotBlank())
            sdfDateTime.parse("$date $time")
        else null
    } catch (_: Exception) {
        null
    }

    private inline fun mutate(block: AddTaskUiState.() -> AddTaskUiState) {
        uiState = uiState.block()
    }
}

/**
 * State を remember して提供
 */
@Composable
fun rememberAddTaskState(
    taskViewModel: TaskViewModel,
    editingEventId: String?,
    calendarMode: CalendarMode?,
    displayedBaseDate: LocalDate?,
    weekStartDate: LocalDate?,
    threeDayStartDate: LocalDate?,
    displayedYearMonth: YearMonth?,
    defaultTaskDurationMinutes: Int,
    onNavigateBackToCalendar: () -> Unit,
    onEditComplete: () -> Unit
): AddTaskState {
    val today = remember { LocalDate.now() }
    return remember(
        taskViewModel,
        editingEventId,
        calendarMode,
        displayedBaseDate,
        weekStartDate,
        threeDayStartDate,
        displayedYearMonth,
        defaultTaskDurationMinutes
    ) {
        AddTaskState(
            initial = AddTaskUiState(
                isEditing = editingEventId != null,
                editingEventId = editingEventId
            ),
            taskViewModel = taskViewModel,
            editingEventId = editingEventId,
            calendarMode = calendarMode,
            displayedBaseDate = displayedBaseDate,
            weekStartDate = weekStartDate,
            threeDayStartDate = threeDayStartDate,
            displayedYearMonth = displayedYearMonth,
            defaultTaskDurationMinutes = defaultTaskDurationMinutes,
            today = today,
            onNavigateBackToCalendar = onNavigateBackToCalendar,
            onEditComplete = onEditComplete
        )
    }
}