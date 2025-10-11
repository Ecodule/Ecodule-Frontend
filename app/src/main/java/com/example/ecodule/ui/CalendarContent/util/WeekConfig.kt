package com.example.ecodule.ui.CalendarContent.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * 週開始日に関するユーティリティと、曜日ヘッダー表示
 */
object WeekConfig {
    /** 日本語の曜日ラベル（固定配列） */
    private val jpWeekdays = listOf("月", "火", "水", "木", "金", "土", "日")

    /** DayOfWeek -> 日本語1文字ラベル */
    fun labelOf(dow: DayOfWeek): String = when (dow) {
        DayOfWeek.MONDAY -> "月"
        DayOfWeek.TUESDAY -> "火"
        DayOfWeek.WEDNESDAY -> "水"
        DayOfWeek.THURSDAY -> "木"
        DayOfWeek.FRIDAY -> "金"
        DayOfWeek.SATURDAY -> "土"
        DayOfWeek.SUNDAY -> "日"
    }

    /** 「土曜日/日曜日/月曜日」などの日本語ラベルを DayOfWeek へ */
    fun toDayOfWeek(jp: String): DayOfWeek = when (jp) {
        "土曜日" -> DayOfWeek.SATURDAY
        "月曜日" -> DayOfWeek.MONDAY
        else -> DayOfWeek.SUNDAY
    }

    /** 指定の週開始日に合わせた曜日順のリストを返す（7要素） */
    fun weekdayOrder(start: DayOfWeek): List<DayOfWeek> {
        // DayOfWeekのvalueは 1=MON..7=SUN
        val ordered = mutableListOf<DayOfWeek>()
        var cur = start
        repeat(7) {
            ordered += cur
            cur = cur.plus(1)
        }
        return ordered
    }

    /** 指定日の「その週の週頭日」を返す（週開始日に合わせて後ろ方向に丸める） */
    fun getStartOfWeek(date: LocalDate, weekStart: DayOfWeek): LocalDate {
        var d = date
        while (d.dayOfWeek != weekStart) {
            d = d.minusDays(1)
        }
        return d
    }

    /** 月の1日が、週開始日に対して何列目に来るか（0..6）を返す */
    fun firstDayCellIndex(yearMonth: YearMonth, weekStart: DayOfWeek): Int {
        val firstDow = yearMonth.atDay(1).dayOfWeek
        // 1..7 を 0..6 に正規化（MON=0 .. SUN=6）
        fun norm(d: DayOfWeek) = (d.value + 6) % 7
        val first = norm(firstDow)
        val start = norm(weekStart)
        return (first - start + 7) % 7
    }
}

/** 月表示用の曜日ヘッダー（左に週数カラムなどのスペースを入れたい場合は leftSpacerWidth を指定） */
@Composable
fun WeekdayHeader(
    weekStart: DayOfWeek,
    leftSpacerWidth: Dp = 0.dp
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFEAEAEA))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leftSpacerWidth > 0.dp) {
            Box(Modifier.padding(start = leftSpacerWidth))
        }
        WeekConfig.weekdayOrder(weekStart).forEach { dow ->
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    WeekConfig.labelOf(dow),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF888888),
                    fontSize = 16.sp
                )
            }
        }
    }
}

/** 任意の日付配列に対する曜日ヘッダー（時間バーや左カラム分のスペースを leftSpacerWidth で与える） */
@Composable
fun WeekdayHeaderForDates(
    dates: List<LocalDate>,
    leftSpacerWidth: Dp = 0.dp
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFEAEAEA))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leftSpacerWidth > 0.dp) {
            Box(Modifier.padding(start = leftSpacerWidth))
        }
        dates.forEach { date ->
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    WeekConfig.labelOf(date.dayOfWeek),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF888888),
                    fontSize = 16.sp
                )
            }
        }
    }
}

/** 週表示用（開始日から7日分）の曜日ヘッダー */
@Composable
fun WeekdayHeaderForWeek(
    weekStartDate: LocalDate,
    leftSpacerWidth: Dp = 0.dp
) {
    val days = (0..6).map { weekStartDate.plusDays(it.toLong()) }
    WeekdayHeaderForDates(dates = days, leftSpacerWidth = leftSpacerWidth)
}

/** 3日表示用（開始日から3日分）の曜日ヘッダー */
@Composable
fun WeekdayHeaderForThreeDays(
    startDay: LocalDate,
    leftSpacerWidth: Dp = 0.dp
) {
    val days = (0..2).map { startDay.plusDays(it.toLong()) }
    WeekdayHeaderForDates(dates = days, leftSpacerWidth = leftSpacerWidth)
}