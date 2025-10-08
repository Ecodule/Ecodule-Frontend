package com.example.ecodule.ui.CalendarContent.model

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import com.example.ecodule.ui.animation.CalendarSlideDirection

data class CalendarUiState(
    val calendarMode: CalendarMode = CalendarMode.MONTH,
    val yearMonth: YearMonth = YearMonth.now(),
    val baseDate: LocalDate = LocalDate.now(),
    val cameFromMonth: Boolean = false,
    val pageDirection: CalendarSlideDirection = CalendarSlideDirection.NONE
)

private const val KEY_MODE = "calendar_mode"
private const val KEY_YEARMONTH = "calendar_yearmonth"
private const val KEY_BASEDATE = "calendar_basedate"
private const val KEY_CAMEFROMMONTH = "calendar_camefrommonth"
private const val KEY_PAGEDIR = "calendar_pagedir"

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CalendarUiState(
            calendarMode = savedStateHandle.get<String>(KEY_MODE)?.let { runCatching { CalendarMode.valueOf(it) }.getOrNull() } ?: CalendarMode.MONTH,
            yearMonth = savedStateHandle.get<String>(KEY_YEARMONTH)?.let { runCatching { YearMonth.parse(it) }.getOrNull() } ?: YearMonth.now(),
            baseDate = savedStateHandle.get<String>(KEY_BASEDATE)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now(),
            cameFromMonth = savedStateHandle.get<Boolean>(KEY_CAMEFROMMONTH) ?: false,
            pageDirection = savedStateHandle.get<String>(KEY_PAGEDIR)?.let { runCatching { CalendarSlideDirection.valueOf(it) }.getOrNull() } ?: CalendarSlideDirection.NONE
        )
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private fun persist(newState: CalendarUiState) {
        savedStateHandle[KEY_MODE] = newState.calendarMode.name
        savedStateHandle[KEY_YEARMONTH] = newState.yearMonth.toString()
        savedStateHandle[KEY_BASEDATE] = newState.baseDate.toString()
        savedStateHandle[KEY_CAMEFROMMONTH] = newState.cameFromMonth
        savedStateHandle[KEY_PAGEDIR] = newState.pageDirection.name
    }

    private fun update(transform: (CalendarUiState) -> CalendarUiState) {
        val newState = transform(_uiState.value)
        _uiState.value = newState
        persist(newState)
    }

    fun setCalendarMode(mode: CalendarMode) = update { st ->
        st.copy(
            calendarMode = mode,
            // DAY 以外に戻る時は cameFromMonth をリセット
            cameFromMonth = if (mode == CalendarMode.DAY) st.cameFromMonth else false,
            pageDirection = CalendarSlideDirection.NONE
        )
    }

    fun setYearMonth(ym: YearMonth, direction: CalendarSlideDirection) = update { st ->
        st.copy(
            yearMonth = ym,
            pageDirection = direction
        )
    }

    fun setBaseDate(date: LocalDate) = update { st ->
        st.copy(baseDate = date)
    }

    fun setCameFromMonth(flag: Boolean) = update { st ->
        st.copy(cameFromMonth = flag)
    }

    fun clearPageDirection() = update { st ->
        st.copy(pageDirection = CalendarSlideDirection.NONE)
    }

    fun onMonthPickerChanged(newYm: YearMonth) {
        update { st ->
            val dir = if (newYm > st.yearMonth) CalendarSlideDirection.LEFT else CalendarSlideDirection.RIGHT
            val adjustedDay = st.baseDate.dayOfMonth.coerceAtMost(newYm.lengthOfMonth())
            st.copy(
                yearMonth = newYm,
                baseDate = newYm.atDay(adjustedDay),
                pageDirection = dir
            )
        }
    }

    fun onDayFromMonthSelected(day: Int) {
        update { st ->
            val newDate = st.yearMonth.atDay(day)
            st.copy(
                baseDate = newDate,
                yearMonth = YearMonth.of(newDate.year, newDate.month),
                calendarMode = CalendarMode.DAY,
                cameFromMonth = true,
                pageDirection = CalendarSlideDirection.NONE
            )
        }
    }

    fun backToMonthFromDay() {
        update { st ->
            st.copy(
                calendarMode = CalendarMode.MONTH,
                yearMonth = YearMonth.of(st.baseDate.year, st.baseDate.month),
                cameFromMonth = false,
                pageDirection = CalendarSlideDirection.NONE
            )
        }
    }

    fun swipeMonth(previous: Boolean) {
        update { st ->
            val newYm = if (previous) st.yearMonth.minusMonths(1) else st.yearMonth.plusMonths(1)
            val adjusted = st.baseDate.dayOfMonth.coerceAtMost(newYm.lengthOfMonth())
            st.copy(
                yearMonth = newYm,
                baseDate = newYm.atDay(adjusted),
                pageDirection = if (previous) CalendarSlideDirection.RIGHT else CalendarSlideDirection.LEFT
            )
        }
    }

    fun swipeDay(previous: Boolean) {
        update { st ->
            val newDate = if (previous) st.baseDate.minusDays(1) else st.baseDate.plusDays(1)
            st.copy(
                baseDate = newDate,
                yearMonth = YearMonth.of(newDate.year, newDate.month),
                pageDirection = if (previous) CalendarSlideDirection.RIGHT else CalendarSlideDirection.LEFT
            )
        }
    }

    fun swipeWeek(previous: Boolean) {
        update { st ->
            val newDate = if (previous) st.baseDate.minusWeeks(1) else st.baseDate.plusWeeks(1)
            st.copy(
                baseDate = newDate,
                yearMonth = YearMonth.of(newDate.year, newDate.month),
                pageDirection = if (previous) CalendarSlideDirection.RIGHT else CalendarSlideDirection.LEFT
            )
        }
    }

    fun swipeThreeDay(previous: Boolean) {
        update { st ->
            val days = if (previous) -3 else 3
            val newDate = st.baseDate.plusDays(days.toLong())
            st.copy(
                baseDate = newDate,
                yearMonth = YearMonth.of(newDate.year, newDate.month),
                pageDirection = if (previous) CalendarSlideDirection.RIGHT else CalendarSlideDirection.LEFT
            )
        }
    }

    /**
     * 今日に戻る
     */
    fun goToToday() {
        val today = LocalDate.now()
        update { st ->
            st.copy(
                baseDate = today,
                yearMonth = YearMonth.of(today.year, today.month),
                pageDirection = CalendarSlideDirection.NONE
            )
        }
    }
}