package com.example.ecodule.ui.CalendarContentui.CalendarContent.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.R
import com.example.ecodule.ui.CalendarContent.model.CalendarMode
import com.example.ecodule.ui.CalendarContent.model.CalendarViewModel
import com.example.ecodule.ui.CalendarContent.model.TaskViewModel
import com.example.ecodule.ui.CalendarContent.ui.DrawCalendarGridLines
import com.example.ecodule.ui.CalendarContent.ui.WeekNumberColumnWidthMonth
import com.example.ecodule.ui.CalendarContent.ui.WheelsMonthPicker
import com.example.ecodule.ui.CalendarContent.ui.TodayReturnButton
import com.example.ecodule.ui.CalendarContent.ui.datedisplay.*
import com.example.ecodule.ui.CalendarContent.util.WeekConfig
import com.example.ecodule.ui.CalendarContent.util.WeekdayHeader
import com.example.ecodule.ui.CalendarContent.util.noRippleClickable
import com.example.ecodule.ui.UserViewModel
import com.example.ecodule.ui.animation.CalendarPagedAnimatedContent
import com.example.ecodule.ui.animation.CalendarPageState
import com.example.ecodule.ui.animation.CalendarSlideDirection
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

// Added imports
import com.example.ecodule.ui.CalendarContent.ui.HourBarWidth
import com.example.ecodule.ui.CalendarContent.util.WeekdayHeaderForThreeDays
import com.example.ecodule.ui.CalendarContent.util.WeekdayHeaderForWeek

fun getCategoryColor(category: String): Color {
    return when (category) {
        "ゴミ出し" -> Color(0xFF2C8FC0)
        "通勤/通学" -> Color(0xFFBD5233)
        "外出" -> Color(0xFF7FAB2A)
        "買い物" -> Color(0xFF1C965B)
        else -> Color(0xFFFF0000)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarContentScreen(
    modifier: Modifier = Modifier,
    calendarViewModel: CalendarViewModel,
    selectedDestination: MutableState<String>,
    onEventClick: (String) -> Unit = {},
    userViewModel: UserViewModel,
    taskViewModel: TaskViewModel,
    showWeekNumbers: Boolean = false,
    weekStart: DayOfWeek = DayOfWeek.SUNDAY,
    onAddTaskRequest: (
        CalendarMode,
        LocalDate,
        LocalDate,
        LocalDate,
        YearMonth
    ) -> Unit = { _, _, _, _, _ -> }
) {
    val ui by calendarViewModel.uiState.collectAsState()

    val calendarMode = ui.calendarMode
    val yearMonth = ui.yearMonth
    val baseDate = ui.baseDate
    val pageDirection = ui.pageDirection
    val cameFromMonth = ui.cameFromMonth

    var showModeDialog by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(refreshing)

    // Derived
    val currentWeekStart = remember(weekStart, baseDate) {
        WeekConfig.getStartOfWeek(baseDate, weekStart)
    }
    val currentThreeDayStart = baseDate.minusDays(1)

    // baseDate が月の日数超過の場合調整
    LaunchedEffect(yearMonth, baseDate) {
        val maxDay = yearMonth.lengthOfMonth()
        if (baseDate.dayOfMonth > maxDay) {
            calendarViewModel.setBaseDate(yearMonth.atDay(maxDay))
        }
    }

    val user by userViewModel.user.collectAsState()
    Log.d("CalendarContentScreen", "Current user: $user")

    DisposableEffect(user?.id) {
        user?.id?.let { uid ->
            taskViewModel.setUserId(uid)
        }
        onDispose { }
    }

    val events by taskViewModel.events.collectAsState()

    val filteredEvents = remember(events, yearMonth, calendarMode, baseDate, weekStart) {
        when (calendarMode) {
            CalendarMode.MONTH -> {
                com.example.ecodule.ui.CalendarContent.util.getDisplayEventsForMonth(events, yearMonth)
            }
            CalendarMode.DAY -> {
                // 繰り返し含むその日分
                val repeated = com.example.ecodule.ui.CalendarContent.util.getDisplayEventsForDay(events, baseDate)
                // その日開始の元イベント（単発も含む）
                val singles = events.filter {
                    it.startDate.year == baseDate.year &&
                            it.startDate.monthValue == baseDate.monthValue &&
                            it.startDate.dayOfMonth == baseDate.dayOfMonth
                }
                // id+開始日時で重複排除
                (repeated + singles).distinctBy { it.id + "_" + it.startDate }
            }
            CalendarMode.WEEK -> {
                // 両方を合成したバージョン
                val repeated = com.example.ecodule.ui.CalendarContent.util.getDisplayEventsForWeek(
                    events.filter { it.repeatOption != "しない" }, currentWeekStart
                )
                val weekEnd = currentWeekStart.plusDays(6)
                val singles = events.filter { event ->
                    event.repeatOption == "しない" &&
                            !event.startDate.toLocalDate().isBefore(currentWeekStart) &&
                            !event.startDate.toLocalDate().isAfter(weekEnd)
                }
                (repeated + singles).distinctBy { it.id + "_" + it.startDate.toLocalDate().toString() }
            }
            CalendarMode.THREE_DAY -> {
                // 両方を合成したバージョン
                val repeated = com.example.ecodule.ui.CalendarContent.util.getDisplayEventsForThreeDays(
                    events.filter { it.repeatOption != "しない" }, currentThreeDayStart
                )
                val threeDayEnd = currentThreeDayStart.plusDays(2)
                val singles = events.filter { event ->
                    event.repeatOption == "しない" &&
                            !event.startDate.toLocalDate().isBefore(currentThreeDayStart) &&
                            !event.startDate.toLocalDate().isAfter(threeDayEnd)
                }
                (repeated + singles).distinctBy { it.id + "_" + it.startDate.toLocalDate().toString() }
            }
            CalendarMode.SCHEDULE -> {
                // スケジュール表示用：繰り返しイベントも展開し、単発分も合成して重複排除
                val repeated = com.example.ecodule.ui.CalendarContent.util.getDisplayEventsForSchedule(
                    events.filter { it.repeatOption != "しない" }, yearMonth
                )
                val singles = events.filter {
                    it.repeatOption == "しない" &&
                            it.startDate.monthValue == yearMonth.monthValue &&
                            it.startDate.year == yearMonth.year
                }
                (repeated + singles).distinctBy { it.id + "_" + it.startDate }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                refreshing = true
                coroutineScope.launch {
                    delay(800)
                    refreshing = false
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (calendarMode == CalendarMode.DAY && cameFromMonth) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "戻る",
                            modifier = Modifier
                                .size(32.dp)
                                .noRippleClickable {
                                    calendarViewModel.backToMonthFromDay()
                                    showModeDialog = false
                                }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "メニュー",
                            modifier = Modifier
                                .size(32.dp)
                                .noRippleClickable { showModeDialog = true }
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    WheelsMonthPicker(
                        currentMonth = yearMonth,
                        onMonthChanged = { newYm ->
                            calendarViewModel.onMonthPickerChanged(newYm)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    TodayReturnButton(
                        size = 40.dp,
                        frameColor = Color(0xFF8C8C8C),
                        dayTextColor = Color(0xFF8C8C8C),
                        onClick = { calendarViewModel.goToToday() }
                    )
                }

                // 月選択ホイール下の曜日ヘッダー
                when (calendarMode) {
                    CalendarMode.MONTH -> {
                        val leftSpacer = if (showWeekNumbers) WeekNumberColumnWidthMonth else 0.dp
                        WeekdayHeader(weekStart = weekStart, leftSpacerWidth = leftSpacer)
                    }
                    CalendarMode.WEEK -> {
                        // 時間バー分ずらして週の曜日を表示
                        WeekdayHeaderForWeek(
                            weekStartDate = currentWeekStart,
                            leftSpacerWidth = HourBarWidth
                        )
                    }
                    CalendarMode.THREE_DAY -> {
                        // 時間バー分ずらして3日間の曜日を表示（開始日に合わせる）
                        WeekdayHeaderForThreeDays(
                            startDay = currentThreeDayStart,
                            leftSpacerWidth = HourBarWidth
                        )
                    }
                    else -> {
                        // DAY と SCHEDULE はここでは表示しない（DAY は画面内ヘッダーに曜日を表示）
                    }
                }

                var dragX by remember { mutableStateOf(0f) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta -> dragX += delta },
                            onDragStopped = {
                                val threshold = 40f
                                if (dragX > threshold) {
                                    when (calendarMode) {
                                        CalendarMode.MONTH -> calendarViewModel.swipeMonth(previous = true)
                                        CalendarMode.DAY -> calendarViewModel.swipeDay(previous = true)
                                        CalendarMode.WEEK -> calendarViewModel.swipeWeek(previous = true)
                                        CalendarMode.THREE_DAY -> calendarViewModel.swipeThreeDay(previous = true)
                                        else -> {}
                                    }
                                } else if (dragX < -threshold) {
                                    when (calendarMode) {
                                        CalendarMode.MONTH -> calendarViewModel.swipeMonth(previous = false)
                                        CalendarMode.DAY -> calendarViewModel.swipeDay(previous = false)
                                        CalendarMode.WEEK -> calendarViewModel.swipeWeek(previous = false)
                                        CalendarMode.THREE_DAY -> calendarViewModel.swipeThreeDay(previous = false)
                                        else -> {}
                                    }
                                }
                                dragX = 0f
                            }
                        )
                ) {
                    val pageKey = when (calendarMode) {
                        CalendarMode.MONTH -> "M_${yearMonth.year}_${yearMonth.monthValue}"
                        CalendarMode.DAY -> "D_${baseDate}"
                        CalendarMode.WEEK -> "W_${currentWeekStart}"
                        CalendarMode.THREE_DAY -> "T3_${currentThreeDayStart}"
                        CalendarMode.SCHEDULE -> "S_${yearMonth.year}_${yearMonth.monthValue}"
                    }

                    CalendarPagedAnimatedContent(
                        pageState = CalendarPageState(pageKey, pageDirection)
                    ) {
                        when (calendarMode) {
                            CalendarMode.MONTH -> {
                                CalendarMonthView(
                                    yearMonth = yearMonth,
                                    events = filteredEvents,
                                    onDayClick = { day ->
                                        calendarViewModel.onDayFromMonthSelected(day)
                                    },
                                    onEventClick = onEventClick,
                                    showWeekNumbers = showWeekNumbers,
                                    weekStart = weekStart
                                )
                            }
                            CalendarMode.WEEK -> {
                                ScrollableWeekDayTimeView(
                                    weekStart = currentWeekStart,
                                    events = filteredEvents,
                                    onDayClick = { },
                                    onEventClick = onEventClick,
                                    showWeekNumbers = showWeekNumbers,
                                    weekStartDay = weekStart
                                )
                            }
                            CalendarMode.DAY -> {
                                ScrollableDayTimeView(
                                    day = baseDate,
                                    events = filteredEvents,
                                    onDayClick = { },
                                    onEventClick = onEventClick,
                                    showWeekNumbers = showWeekNumbers,
                                    weekStartDay = weekStart
                                )
                            }
                            CalendarMode.THREE_DAY -> {
                                ScrollableThreeDayTimeView(
                                    startDay = currentThreeDayStart,
                                    events = filteredEvents,
                                    onDayClick = { },
                                    onEventClick = onEventClick,
                                    showWeekNumbers = showWeekNumbers,
                                    weekStartDay = weekStart
                                )
                            }
                            CalendarMode.SCHEDULE -> {
                                CalendarScheduleView(
                                    yearMonth = yearMonth,
                                    events = filteredEvents.sortedBy { it.startDate },
                                    onDayClick = { },
                                    onEventClick = onEventClick
                                )
                            }
                        }

                        if (calendarMode == CalendarMode.MONTH && !showWeekNumbers) {
                            DrawCalendarGridLines(
                                rowCount = ((WeekConfig.firstDayCellIndex(yearMonth, weekStart) + yearMonth.lengthOfMonth() + 6) / 7),
                                colCount = 7
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                onAddTaskRequest(
                    calendarMode,
                    baseDate,
                    currentWeekStart,
                    currentThreeDayStart,
                    yearMonth
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ecodule_icon_addtask),
                contentDescription = "予定追加",
                modifier = Modifier.size(64.dp),
                tint = Color.Unspecified
            )
        }

        AnimatedVisibility(visible = showModeDialog) {
            CalendarModeDialog(
                currentMode = calendarMode,
                onModeSelected = { mode ->
                    calendarViewModel.setCalendarMode(mode)
                    if (mode != CalendarMode.DAY) {
                        calendarViewModel.setCameFromMonth(false)
                    }
                    showModeDialog = false
                },
                onDismiss = { showModeDialog = false }
            )
        }
    }
}

@Composable
fun CalendarModeDialog(
    currentMode: CalendarMode,
    onModeSelected: (CalendarMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("カレンダー", fontSize = 20.sp)
            }
        },
        text = {
            Column {
                CalendarMode.values().forEach { mode ->
                    val selected = mode == currentMode
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(if (selected) Color(0xFF1565C0).copy(alpha = 0.18f) else Color.Transparent)
                            .noRippleClickable { onModeSelected(mode) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = mode.icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(18.dp))
                        Text(
                            mode.label,
                            fontSize = 17.sp,
                            color = if (selected) Color(0xFF1565C0) else Color.Unspecified
                        )
                    }
                }
            }
        }
    )
}