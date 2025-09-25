package com.example.ecodule.ui.CalendarContentui.CalendarContent.screen

//import com.example.ecodule.ui.AddTaskContent
import android.app.Application
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.model.CalendarMode
import com.example.ecodule.ui.CalendarContent.ui.CalendarMonthView
import com.example.ecodule.ui.CalendarContent.ui.CalendarScheduleView
import com.example.ecodule.ui.CalendarContent.ui.DrawCalendarGridLines
import com.example.ecodule.ui.CalendarContent.ui.ScrollableDayTimeView
import com.example.ecodule.ui.CalendarContent.ui.ScrollableThreeDayTimeView
import com.example.ecodule.ui.CalendarContent.ui.ScrollableWeekDayTimeView
import com.example.ecodule.ui.CalendarContent.util.getDisplayEventsForMonth
import com.example.ecodule.ui.CalendarContent.util.getStartOfWeek
import com.example.ecodule.ui.CalendarContent.util.noRippleClickable
import com.example.ecodule.ui.EcoduleRoute
import com.example.ecodule.ui.UserViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    userViewModel: UserViewModel
) {
    var yearMonth by remember { mutableStateOf(initialYearMonth) }
    var calendarMode by remember { mutableStateOf(CalendarMode.MONTH) }
    var showModeDialog by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(refreshing)

    // 表示中の基準日を保持（yearMonthと連動）
    var baseDate by remember { mutableStateOf(yearMonth.atDay(LocalDate.now().dayOfMonth.coerceAtMost(yearMonth.lengthOfMonth()))) }

    // 各モードでの表示日を計算（remember + derivedStateOf）
    val currentDay by remember {
        derivedStateOf {
            when (calendarMode) {
                CalendarMode.DAY -> baseDate
                else -> baseDate
            }
        }
    }

    val currentWeekStart by remember {
        derivedStateOf {
            when (calendarMode) {
                CalendarMode.WEEK -> getStartOfWeek(baseDate)
                else -> getStartOfWeek(baseDate)
            }
        }
    }

    val currentThreeDayStart by remember {
        derivedStateOf {
            when (calendarMode) {
                CalendarMode.THREE_DAY -> baseDate.minusDays(1)
                else -> baseDate.minusDays(1)
            }
        }
    }

    // yearMonthが変更された時にbaseDateを更新
    LaunchedEffect(yearMonth) {
        val newBaseDate = yearMonth.atDay(baseDate.dayOfMonth.coerceAtMost(yearMonth.lengthOfMonth()))
        baseDate = newBaseDate
    }

    // 年号付き月表示
    val currentYear = LocalDate.now().year
    val showYear = yearMonth.year != currentYear
    val monthLabel = if (showYear) "${yearMonth.year}年${yearMonth.month.value}月" else "${yearMonth.month.value}月"
    val nextMonth = yearMonth.plusMonths(1)
    val nextMonthLabel = if (showYear || nextMonth.year != currentYear) "${nextMonth.year}年${nextMonth.month.value}月" else "${nextMonth.month.value}月"

    val user = userViewModel.user.collectAsState().value
    Log.d("CalendarContentScreen", "Current user: $user")

    // 表示範囲の予定をフィルタリング（LocalDateTimeベース）
    val filteredEvents = remember(events, yearMonth, calendarMode, baseDate) {
        when (calendarMode) {
            CalendarMode.MONTH -> {
                // 変更点：繰り返し含む表示用イベントリスト
                getDisplayEventsForMonth(events, yearMonth)
            }
            CalendarMode.DAY -> {
                events.filter {
                    it.startDate.monthValue == currentDay.monthValue &&
                            it.startDate.dayOfMonth == currentDay.dayOfMonth &&
                            it.startDate.year == currentDay.year
                }
            }
            CalendarMode.WEEK -> {
                val weekEnd = currentWeekStart.plusDays(6)
                events.filter { event ->
                    val eventDate = event.startDate.toLocalDate()
                    !eventDate.isBefore(currentWeekStart) && !eventDate.isAfter(weekEnd)
                }
            }
            CalendarMode.THREE_DAY -> {
                val threeDayEnd = currentThreeDayStart.plusDays(2)
                events.filter { event ->
                    val eventDate = event.startDate.toLocalDate()
                    !eventDate.isBefore(currentThreeDayStart) && !eventDate.isAfter(threeDayEnd)
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
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "メニュー",
                        modifier = Modifier
                            .size(32.dp)
                            .noRippleClickable { showModeDialog = true }
                    )
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

                // 曜日ヘッダー
                if (calendarMode == CalendarMode.MONTH) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEAEAEA))
                            .padding(vertical = 4.dp)
                    ) {
                        listOf("日", "月", "火", "水", "木", "金", "土").forEach { day ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    day,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF888888),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                // カレンダー本体
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .pointerInput(calendarMode, yearMonth, baseDate) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                when (calendarMode) {
                                    CalendarMode.MONTH -> {
                                        if (dragAmount > 40) {
                                            yearMonth = yearMonth.minusMonths(1)
                                        }
                                        if (dragAmount < -40) {
                                            yearMonth = yearMonth.plusMonths(1)
                                        }
                                    }
                                    CalendarMode.DAY -> {
                                        if (dragAmount > 40) {
                                            val newDate = baseDate.minusDays(1)
                                            baseDate = newDate
                                            yearMonth = YearMonth.of(newDate.year, newDate.month)
                                        }
                                        if (dragAmount < -40) {
                                            val newDate = baseDate.plusDays(1)
                                            baseDate = newDate
                                            yearMonth = YearMonth.of(newDate.year, newDate.month)
                                        }
                                    }
                                    CalendarMode.WEEK -> {
                                        if (dragAmount > 40) {
                                            val newDate = baseDate.minusWeeks(1)
                                            baseDate = newDate
                                            yearMonth = YearMonth.of(newDate.year, newDate.month)
                                        }
                                        if (dragAmount < -40) {
                                            val newDate = baseDate.plusWeeks(1)
                                            baseDate = newDate
                                            yearMonth = YearMonth.of(newDate.year, newDate.month)
                                        }
                                    }
                                    CalendarMode.THREE_DAY -> {
                                        if (dragAmount > 40) {
                                            val newDate = baseDate.minusDays(3)
                                            baseDate = newDate
                                            yearMonth = YearMonth.of(newDate.year, newDate.month)
                                        }
                                        if (dragAmount < -40) {
                                            val newDate = baseDate.plusDays(3)
                                            baseDate = newDate
                                            yearMonth = YearMonth.of(newDate.year, newDate.month)
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                ) {
                    when (calendarMode) {
                        CalendarMode.MONTH -> {
                            CalendarMonthView(
                                yearMonth = yearMonth,
                                events = filteredEvents,
                                onDayClick = { day -> selectedDay = day },
                                onEventClick = onEventClick
                            )
                        }
                        CalendarMode.WEEK -> {
                            ScrollableWeekDayTimeView(
                                weekStart = currentWeekStart,
                                events = filteredEvents,
                                onDayClick = { day -> selectedDay = day },
                                onEventClick = onEventClick
                            )
                        }
                        CalendarMode.DAY -> {
                            ScrollableDayTimeView(
                                day = currentDay,
                                events = filteredEvents,
                                onDayClick = { date -> selectedDay = date.dayOfMonth },
                                onEventClick = onEventClick
                            )
                        }
                        CalendarMode.THREE_DAY -> {
                            ScrollableThreeDayTimeView(
                                startDay = currentThreeDayStart,
                                events = filteredEvents,
                                onDayClick = { day -> selectedDay = day },
                                onEventClick = onEventClick
                            )
                        }
                        CalendarMode.SCHEDULE -> {
                            CalendarScheduleView(
                                yearMonth = yearMonth,
                                events = filteredEvents.sortedBy { it.startDate },
                                onDayClick = { day -> selectedDay = day },
                                onEventClick = onEventClick
                            )
                        }
                    }
                    if (calendarMode == CalendarMode.MONTH) {
                        DrawCalendarGridLines(
                            rowCount = ((yearMonth.atDay(1).dayOfWeek.value % 7 + yearMonth.lengthOfMonth() + 6) / 7),
                            colCount = 7
                        )
                    }
                }
            }
        }

        // 予定追加ボタン
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

        // 表示切替ダイアログ
        AnimatedVisibility(visible = showModeDialog) {
            CalendarModeDialog(
                currentMode = calendarMode,
                onModeSelected = { mode ->
                    calendarMode = mode
                    showModeDialog = false
                },
                onDismiss = { showModeDialog = false }
            )
        }

        // 日付クリック時の予定一覧ダイアログ（月表示のみで反応）
        if (selectedDay != null && calendarMode == CalendarMode.MONTH) {
            AlertDialog(
                onDismissRequest = { selectedDay = null },
                confirmButton = {
                    TextButton(onClick = { selectedDay = null }) {
                        Text("閉じる")
                    }
                },
                title = { Text("${monthLabel}${selectedDay}日の予定") },
                text = {
                    val plans = filteredEvents.filter { it.startDate.dayOfMonth == selectedDay }
                    if (plans.isEmpty()) {
                        Text("予定はありません")
                    } else {
                        LazyColumn {
                            items(plans.size) { index ->
                                val event = plans[index]
                                TextButton(
                                    onClick = {
                                        selectedDay = null
                                        onEventClick(event.id)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "・${event.label} ${event.timeRangeText}",
                                        color = event.color
                                    )
                                }
                            }
                        }
                    }
                }
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

@Preview(showBackground = true)
@Composable
fun CalendarContentPreview() {
    val context = LocalContext.current
    val dummySelectedDestination = remember { mutableStateOf("Calendar") }
    val dummyUserViewModel: UserViewModel = remember { UserViewModel(context.applicationContext as Application) }
    CalendarContentScreen(selectedDestination = dummySelectedDestination, userViewModel = dummyUserViewModel)
}