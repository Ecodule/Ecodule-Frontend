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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.model.CalendarMode
import com.example.ecodule.ui.CalendarContent.ui.DrawCalendarGridLines
import com.example.ecodule.ui.CalendarContent.ui.datedisplay.*
import com.example.ecodule.ui.CalendarContent.ui.WeekNumberColumnWidthMonth
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarContentScreen(
    modifier: Modifier = Modifier,
    initialYearMonth: YearMonth = YearMonth.now(),
    selectedDestination: MutableState<String>,
    events: List<CalendarEvent> = emptyList(),
    onEventClick: (String) -> Unit = {},
    userViewModel: UserViewModel,
    // 追加: 設定から受け取る週設定
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

    // 表示基準日（yearMonth と連動）
    var baseDate by remember {
        mutableStateOf(
            yearMonth.atDay(LocalDate.now().dayOfMonth.coerceAtMost(yearMonth.lengthOfMonth()))
        )
    }

    // 週開始日に合わせた週頭日・3日開始日
    val currentDay by remember { derivedStateOf { baseDate } }
    val currentWeekStart by remember(weekStart, baseDate) {
        derivedStateOf { WeekConfig.getStartOfWeek(baseDate, weekStart) }
    }
    val currentThreeDayStart by remember { derivedStateOf { baseDate.minusDays(1) } }

    // yearMonthが変更された時にbaseDateを更新
    LaunchedEffect(yearMonth) {
        baseDate = yearMonth.atDay(baseDate.dayOfMonth.coerceAtMost(yearMonth.lengthOfMonth()))
    }

    // 年号付き月表示
    val currentYear = LocalDate.now().year
    val showYear = yearMonth.year != currentYear
    val monthLabel = if (showYear) "${yearMonth.year}年${yearMonth.month.value}月" else "${yearMonth.month.value}月"
    val nextMonth = yearMonth.plusMonths(1)
    val nextMonthLabel = if (showYear || nextMonth.year != currentYear) "${nextMonth.year}年${nextMonth.month.value}月" else "${nextMonth.month.value}月"

    val user by userViewModel.user.collectAsState()
    Log.d("CalendarContentScreen", "Current user: $user")

    // 表示範囲の予定をフィルタリング
    val filteredEvents = remember(events, yearMonth, calendarMode, baseDate, weekStart) {
        when (calendarMode) {
            CalendarMode.MONTH -> {
                // 月全体（繰り返し含める場合は既存ヘルパーを利用）
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
                                .pointerInput(Unit) {}
                                .composed { this }
                                .let { it }
                                .padding(0.dp)
                                .width(32.dp)
                                .height(32.dp)
                                .then(
                                    Modifier
                                        .padding(0.dp)
                                        .width(32.dp)
                                )
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
                    Text(
                        monthLabel,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF444444)
                    )
                    if (calendarMode == CalendarMode.MONTH) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            nextMonthLabel,
                            fontSize = 22.sp,
                            color = Color(0xFFB0B0B0)
                        )
                    }
                }

                // 曜日ヘッダー（月表示のみ）
                if (calendarMode == CalendarMode.MONTH) {
                    val leftSpacer = if (showWeekNumbers) WeekNumberColumnWidthMonth else 0.dp
                    WeekdayHeader(weekStart = weekStart, leftSpacerWidth = leftSpacer)
                }

                // 本体
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

        // 予定追加ボタン等（既存）
        FloatingActionButton(
            onClick = { selectedDestination.value = EcoduleRoute.TASKS },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF88C057)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "予定追加",
                modifier = Modifier.size(36.dp),
                tint = Color.White
            )
        }

        // 表示切替ダイアログ（既存）
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

/*
@Preview(showBackground = true)
@Composable
fun CalendarContentPreview() {
    val context = LocalContext.current
    val dummySelectedDestination = remember { mutableStateOf("Calendar") }
    val dummyUserViewModel: UserViewModel = hiltViewModel()
    CalendarContentScreen(selectedDestination = dummySelectedDestination, userViewModel = dummyUserViewModel)
}
 */