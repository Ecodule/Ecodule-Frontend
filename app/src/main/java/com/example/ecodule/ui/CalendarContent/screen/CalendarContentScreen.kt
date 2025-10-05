package com.example.ecodule.ui.CalendarContentui.CalendarContent.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.painterResource
import com.example.ecodule.R
import com.example.ecodule.ui.CalendarContent.model.CalendarMode
import com.example.ecodule.ui.CalendarContent.model.TaskViewModel
import com.example.ecodule.ui.CalendarContent.ui.DrawCalendarGridLines
import com.example.ecodule.ui.CalendarContent.ui.datedisplay.*
import com.example.ecodule.ui.CalendarContent.ui.WeekNumberColumnWidthMonth
import com.example.ecodule.ui.CalendarContent.ui.WheelsMonthPicker
import com.example.ecodule.ui.CalendarContent.util.WeekConfig
import com.example.ecodule.ui.CalendarContent.util.WeekdayHeader
import com.example.ecodule.ui.CalendarContent.util.noRippleClickable
import com.example.ecodule.ui.EcoduleRoute
import com.example.ecodule.ui.UserViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

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
    initialYearMonth: YearMonth = YearMonth.now(),
    selectedDestination: MutableState<String>,
    onEventClick: (String) -> Unit = {},
    userViewModel: UserViewModel,
    taskViewModel: TaskViewModel,
    showWeekNumbers: Boolean = false,
    weekStart: DayOfWeek = DayOfWeek.SUNDAY
) {
    var yearMonth by remember { mutableStateOf(initialYearMonth) }
    var calendarMode by remember { mutableStateOf(CalendarMode.MONTH) }
    var showModeDialog by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(refreshing)

    var cameFromMonth by remember { mutableStateOf(false) }

    var baseDate by remember {
        mutableStateOf(
            yearMonth.atDay(LocalDate.now().dayOfMonth.coerceAtMost(yearMonth.lengthOfMonth()))
        )
    }

    val currentDay by remember { derivedStateOf { baseDate } }
    val currentWeekStart by remember(weekStart, baseDate) {
        derivedStateOf { WeekConfig.getStartOfWeek(baseDate, weekStart) }
    }
    val currentThreeDayStart by remember { derivedStateOf { baseDate.minusDays(1) } }

    LaunchedEffect(yearMonth) {
        baseDate = yearMonth.atDay(baseDate.dayOfMonth.coerceAtMost(yearMonth.lengthOfMonth()))
    }

    val currentYear = LocalDate.now().year
    val showYear = yearMonth.year != currentYear
    val monthLabel = if (showYear) "${yearMonth.year}年${yearMonth.month.value}月" else "${yearMonth.month.value}月"
    val nextMonth = yearMonth.plusMonths(1)
    val nextMonthLabel = if (showYear || nextMonth.year != currentYear) "${nextMonth.year}年${nextMonth.month.value}月" else "${nextMonth.month.value}月"

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
                events.filter {
                    it.startDate.year == currentDay.year &&
                            it.startDate.monthValue == currentDay.monthValue &&
                            it.startDate.dayOfMonth == currentDay.dayOfMonth
                }
            }
            CalendarMode.WEEK -> {
                val weekEnd = currentWeekStart.plusDays(6)
                events.filter { event ->
                    val d = event.startDate.toLocalDate()
                    !d.isBefore(currentWeekStart) && !d.isAfter(weekEnd)
                }
            }
            CalendarMode.THREE_DAY -> {
                val threeDayEnd = currentThreeDayStart.plusDays(2)
                events.filter { event ->
                    val d = event.startDate.toLocalDate()
                    !d.isBefore(currentThreeDayStart) && !d.isAfter(threeDayEnd)
                }
            }
            CalendarMode.SCHEDULE -> {
                events.filter { it.startDate.monthValue == yearMonth.monthValue && it.startDate.year == yearMonth.year }
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
                // 上部バー
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    if (calendarMode == CalendarMode.DAY && cameFromMonth) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "戻る",
                            modifier = Modifier
                                .size(32.dp)
                                .noRippleClickable {
                                    calendarMode = CalendarMode.MONTH
                                    yearMonth = YearMonth.of(baseDate.year, baseDate.month)
                                    cameFromMonth = false
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

                    // ドラムロール（月スワイプ）
                    WheelsMonthPicker(
                        currentMonth = yearMonth,
                        onMonthChanged = { newYm ->
                            // ヘッダーのスワイプで月変更 -> 本体状態に反映
                            yearMonth = newYm
                            // 現在の baseDate の日を保ちながら、範囲内に補正
                            baseDate = newYm.atDay(baseDate.dayOfMonth.coerceAtMost(newYm.lengthOfMonth()))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 曜日ヘッダー（月表示のみ）
                if (calendarMode == CalendarMode.MONTH) {
                    val leftSpacer = if (showWeekNumbers) WeekNumberColumnWidthMonth else 0.dp
                    WeekdayHeader(weekStart = weekStart, leftSpacerWidth = leftSpacer)
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
                                when (calendarMode) {
                                    CalendarMode.MONTH -> {
                                        if (dragX > threshold) yearMonth = yearMonth.minusMonths(1)
                                        else if (dragX < -threshold) yearMonth = yearMonth.plusMonths(1)
                                    }
                                    CalendarMode.DAY -> {
                                        if (dragX > threshold) {
                                            val newDate = baseDate.minusDays(1)
                                            baseDate = newDate
                                            yearMonth = YearMonth.of(newDate.year, newDate.month)
                                        } else if (dragX < -threshold) {
                                            val newDate = baseDate.plusDays(1)
                                            baseDate = newDate
                                            yearMonth = YearMonth.of(newDate.year, newDate.month)
                                        }
                                    }
                                    CalendarMode.WEEK -> {
                                        if (dragX > threshold) {
                                            val newDate = baseDate.minusWeeks(1)
                                            baseDate = newDate
                                            yearMonth = YearMonth.of(newDate.year, newDate.month)
                                        } else if (dragX < -threshold) {
                                            val newDate = baseDate.plusWeeks(1)
                                            baseDate = newDate
                                            yearMonth = YearMonth.of(newDate.year, newDate.month)
                                        }
                                    }
                                    CalendarMode.THREE_DAY -> {
                                        if (dragX > threshold) {
                                            val newDate = baseDate.minusDays(3)
                                            baseDate = newDate
                                            yearMonth = YearMonth.of(newDate.year, newDate.month)
                                        } else if (dragX < -threshold) {
                                            val newDate = baseDate.plusDays(3)
                                            baseDate = newDate
                                            yearMonth = YearMonth.of(newDate.year, newDate.month)
                                        }
                                    }
                                    else -> {}
                                }
                                dragX = 0f
                            }
                        )
                ) {
                    when (calendarMode) {
                        CalendarMode.MONTH -> {
                            CalendarMonthView(
                                yearMonth = yearMonth,
                                events = filteredEvents,
                                onDayClick = { day ->
                                    val newDate = yearMonth.atDay(day)
                                    baseDate = newDate
                                    yearMonth = YearMonth.of(newDate.year, newDate.month)
                                    calendarMode = CalendarMode.DAY
                                    cameFromMonth = true
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
                                onDayClick = { /* no-op */ },
                                onEventClick = onEventClick,
                                showWeekNumbers = showWeekNumbers,
                                weekStartDay = weekStart
                            )
                        }
                        CalendarMode.DAY -> {
                            ScrollableDayTimeView(
                                day = currentDay,
                                events = filteredEvents,
                                onDayClick = { /* no-op */ },
                                onEventClick = onEventClick,
                                showWeekNumbers = showWeekNumbers,
                                weekStartDay = weekStart
                            )
                        }
                        CalendarMode.THREE_DAY -> {
                            ScrollableThreeDayTimeView(
                                startDay = currentThreeDayStart,
                                events = filteredEvents,
                                onDayClick = { /* no-op */ },
                                onEventClick = onEventClick,
                                showWeekNumbers = showWeekNumbers,
                                weekStartDay = weekStart
                            )
                        }
                        CalendarMode.SCHEDULE -> {
                            CalendarScheduleView(
                                yearMonth = yearMonth,
                                events = filteredEvents.sortedBy { it.startDate },
                                onDayClick = { /* no-op */ },
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

        // 予定追加ボタン（背景と影を消し、葉っぱのみ表示）
        FloatingActionButton(
            onClick = { selectedDestination.value = EcoduleRoute.TASKS },
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
                    calendarMode = mode
                    showModeDialog = false
                    if (mode != CalendarMode.DAY) {
                        cameFromMonth = false
                    }
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
                Text("カレンダー", fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color(0xFF1565C0) else Color.Unspecified
                        )
                    }
                }
            }
        }
    )
}