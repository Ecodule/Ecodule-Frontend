package com.example.ecodule.ui.CalendarContentui.CalendarContent.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.ui.CalendarContent.ui.CalendarMonthView
import com.example.ecodule.ui.CalendarContent.ui.CalendarScheduleView
import com.example.ecodule.ui.CalendarContent.ui.DrawCalendarGridLines
import com.example.ecodule.ui.CalendarContent.ui.ScrollableDayTimeView
import com.example.ecodule.ui.CalendarContent.ui.ScrollableThreeDayTimeView
import com.example.ecodule.ui.CalendarContent.ui.ScrollableWeekDayTimeView
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.model.CalendarMode
import com.example.ecodule.ui.CalendarContent.util.noRippleClickable
import com.example.ecodule.ui.CalendarContent.util.getStartOfWeek
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarContentScreen(
    modifier: Modifier = Modifier,
    initialYearMonth: YearMonth = YearMonth.now(),
    events: List<CalendarEvent> = listOf(
        CalendarEvent(25, "企画進捗", Color(0xFF81C784), 7),
        CalendarEvent(29, "買い物", Color(0xFFE57373), 7),
        CalendarEvent(10, "買い物", Color(0xFFE57373), 7, 10, 11), // 動作テスト用（10:00-11:00）
    ),
) {
    var yearMonth by remember { mutableStateOf(initialYearMonth) }
    var calendarMode by remember { mutableStateOf(CalendarMode.MONTH) }
    var showModeDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(refreshing)

    // 1日・週・3日スクロール制御
    var currentDay by remember { mutableStateOf(LocalDate.of(yearMonth.year, yearMonth.month, 1)) }
    var currentWeekStart by remember { mutableStateOf(
        getStartOfWeek(
            LocalDate.of(
                yearMonth.year,
                yearMonth.month,
                1
            )
        )
    ) }
    var currentThreeDayStart by remember { mutableStateOf(LocalDate.of(yearMonth.year, yearMonth.month, 1)) }

    // 年号付き月表示
    val currentYear = LocalDate.now().year
    val showYear = yearMonth.year != currentYear
    val monthLabel = if (showYear) "${yearMonth.year}年${yearMonth.month.value}月" else "${yearMonth.month.value}月"
    val nextMonth = yearMonth.plusMonths(1)
    val nextMonthLabel = if (showYear || nextMonth.year != currentYear) "${nextMonth.year}年${nextMonth.month.value}月" else "${nextMonth.month.value}月"

    // 月またぎ許可: 1日・週・3日で状態が月をまたぐ場合は自動的にyearMonthを更新
    LaunchedEffect(currentDay) {
        if (calendarMode == CalendarMode.DAY) {
            val ym = YearMonth.of(currentDay.year, currentDay.month)
            if (ym != yearMonth) yearMonth = ym
        }
    }
    LaunchedEffect(currentWeekStart) {
        if (calendarMode == CalendarMode.WEEK) {
            val ym = YearMonth.of(currentWeekStart.year, currentWeekStart.month)
            if (ym != yearMonth) yearMonth = ym
        }
    }
    LaunchedEffect(currentThreeDayStart) {
        if (calendarMode == CalendarMode.THREE_DAY) {
            val ym = YearMonth.of(currentThreeDayStart.year, currentThreeDayStart.month)
            if (ym != yearMonth) yearMonth = ym
        }
    }

    // 7月の予定だけ表示
    val filteredEvents = events.filter { it.month == 7 }

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
                        .pointerInput(calendarMode, yearMonth, currentDay, currentWeekStart, currentThreeDayStart) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                when (calendarMode) {
                                    CalendarMode.MONTH -> {
                                        if (dragAmount > 40) yearMonth = yearMonth.minusMonths(1)
                                        if (dragAmount < -40) yearMonth = yearMonth.plusMonths(1)
                                    }
                                    CalendarMode.DAY -> {
                                        if (dragAmount > 40) currentDay = currentDay.minusDays(1)
                                        if (dragAmount < -40) currentDay = currentDay.plusDays(1)
                                    }
                                    CalendarMode.WEEK -> {
                                        if (dragAmount > 40) currentWeekStart = currentWeekStart.minusWeeks(1)
                                        if (dragAmount < -40) currentWeekStart = currentWeekStart.plusWeeks(1)
                                    }
                                    CalendarMode.THREE_DAY -> {
                                        if (dragAmount > 40) currentThreeDayStart = currentThreeDayStart.minusDays(3)
                                        if (dragAmount < -40) currentThreeDayStart = currentThreeDayStart.plusDays(3)
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
                                onDayClick = { day -> selectedDay = day }
                            )
                        }
                        CalendarMode.WEEK -> {
                            ScrollableWeekDayTimeView(
                                weekStart = currentWeekStart,
                                events = filteredEvents,
                                onDayClick = { day -> selectedDay = day }
                            )
                        }
                        CalendarMode.DAY -> {
                            ScrollableDayTimeView(
                                day = currentDay,
                                events = filteredEvents,
                                onDayClick = { date -> selectedDay = date.dayOfMonth }
                            )
                        }
                        CalendarMode.THREE_DAY -> {
                            ScrollableThreeDayTimeView(
                                startDay = currentThreeDayStart,
                                events = filteredEvents,
                                onDayClick = { day -> selectedDay = day }
                            )
                        }
                        CalendarMode.SCHEDULE -> {
                            CalendarScheduleView(
                                yearMonth = yearMonth,
                                events = filteredEvents,
                                onDayClick = { day -> selectedDay = day }
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
            onClick = { showAddTaskDialog = true },
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
        // タスク追加ダイアログ
        if (showAddTaskDialog) {
            AlertDialog(
                onDismissRequest = { showAddTaskDialog = false },
                confirmButton = {
                    TextButton(onClick = { showAddTaskDialog = false }) {
                        Text("OK")
                    }
                },
                title = { Text("タスク追加") },
                text = { Text("これはタスク追加ポップアップです") }
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
                    val plans = filteredEvents.filter { it.day == selectedDay }
                    if (plans.isEmpty()) {
                        Text("予定はありません")
                    } else {
                        Column {
                            plans.forEach {
                                val timeText = if (it.startHour != null && it.endHour != null) "（${it.startHour}:00〜${it.endHour}:00）" else ""
                                Text("・${it.label} $timeText", color = it.color)
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