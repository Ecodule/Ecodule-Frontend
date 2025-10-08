package com.example.ecodule.ui.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.YearMonth

/**
 * カレンダー内部（左右スワイプ）ページ切替アニメーション。
 * Google Calendar ライクに左右スライド + フェード。
 */
enum class CalendarSlideDirection { LEFT, RIGHT, NONE }

@Immutable
data class CalendarPageState<T>(
    val key: T,
    val direction: CalendarSlideDirection
)

/**
 * direction の意味:
 *  LEFT  : 未来（次へ）進む。現在表示 → 左へ抜け、新ページが右から入る。
 *  RIGHT : 過去（前へ）戻る。現在表示 → 右へ抜け、新ページが左から入る。
 *  NONE  : フェードのみ。
 *
 * 追加仕様:
 *  - initial / target 双方の direction が NONE で、かつキー prefix（表示モード識別）が同じ
 *    （例: 月→月, 日→日 等）かつ key が変化している場合は、日付・月の前後関係から
 *    自動的に LEFT / RIGHT を推測しスライドさせる。
 *    → TodayReturnButton で今日へジャンプする際など、明示方向が設定されていないケースにも
 *       スライド演出を適用。
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T> CalendarPagedAnimatedContent(
    pageState: CalendarPageState<T>,
    content: @Composable (T) -> Unit
) {
    val duration = 260
    AnimatedContent(
        targetState = pageState,
        transitionSpec = {
            val explicitDir = targetState.direction
            val effDir = inferDirectionIfNeeded(initialState, targetState, explicitDir)

            val enter = when (effDir) {
                CalendarSlideDirection.LEFT -> {
                    // 次へ: 新ページは右から
                    slideInHorizontally(
                        animationSpec = tween(duration, easing = FastOutSlowInEasing),
                        initialOffsetX = { full -> full / 2 }
                    ) + fadeIn(tween(duration))
                }
                CalendarSlideDirection.RIGHT -> {
                    // 戻る: 新ページは左から
                    slideInHorizontally(
                        animationSpec = tween(duration, easing = FastOutSlowInEasing),
                        initialOffsetX = { full -> -full / 2 }
                    ) + fadeIn(tween(duration))
                }
                CalendarSlideDirection.NONE -> {
                    fadeIn(tween(150))
                }
            }

            val exit = when (effDir) {
                CalendarSlideDirection.LEFT -> {
                    // 旧ページは左へ
                    slideOutHorizontally(
                        animationSpec = tween(duration),
                        targetOffsetX = { full -> -full / 2 }
                    ) + fadeOut(tween(duration - 40))
                }
                CalendarSlideDirection.RIGHT -> {
                    // 旧ページは右へ
                    slideOutHorizontally(
                        animationSpec = tween(duration),
                        targetOffsetX = { full -> full / 2 }
                    ) + fadeOut(tween(duration - 40))
                }
                CalendarSlideDirection.NONE -> {
                    fadeOut(tween(120))
                }
            }

            ContentTransform(
                targetContentEnter = enter,
                initialContentExit = exit,
                sizeTransform = SizeTransform(clip = false)
            )
        },
        label = "CalendarPageAnimatedContent"
    ) { state ->
        content(state.key)
    }
}

/* =========================================================
 * 方向自動推測ロジック
 * ========================================================= */

/**
 * direction が NONE のままでも、同モード内でページが変化している場合は
 * キー文字列から時間的前後を判定し LEFT / RIGHT を自動決定。
 */
private fun <T> inferDirectionIfNeeded(
    initial: CalendarPageState<T>,
    target: CalendarPageState<T>,
    explicitTargetDir: CalendarSlideDirection
): CalendarSlideDirection {
    // 明示指定があればそれを優先
    if (explicitTargetDir != CalendarSlideDirection.NONE) return explicitTargetDir
    if (initial.key == target.key) return CalendarSlideDirection.NONE

    val initialKey = initial.key.toString()
    val targetKey = target.key.toString()

    // キー接頭辞（表示モード）: 'M_', 'D_', 'W_', 'T3_', 'S_'
    val prefixInitial = extractPrefix(initialKey)
    val prefixTarget = extractPrefix(targetKey)

    // 異なるモード (例: 月->日) の場合は従来通りフェード
    if (prefixInitial != prefixTarget) return CalendarSlideDirection.NONE

    // 同モードで比較
    return when (prefixInitial) {
        "M", "S" -> { // Month / Schedule
            val im = parseYearMonth(initialKey)
            val tm = parseYearMonth(targetKey)
            if (im != null && tm != null) {
                if (tm > im) CalendarSlideDirection.LEFT else CalendarSlideDirection.RIGHT
            } else CalendarSlideDirection.NONE
        }
        "D" -> {
            val id = parseLocalDate(initialKey)
            val td = parseLocalDate(targetKey)
            if (id != null && td != null) {
                if (td > id) CalendarSlideDirection.LEFT else CalendarSlideDirection.RIGHT
            } else CalendarSlideDirection.NONE
        }
        "W" -> {
            val iw = parseLocalDate(initialKey)
            val tw = parseLocalDate(targetKey)
            if (iw != null && tw != null) {
                if (tw > iw) CalendarSlideDirection.LEFT else CalendarSlideDirection.RIGHT
            } else CalendarSlideDirection.NONE
        }
        "T3" -> {
            val it3 = parseLocalDate(initialKey)
            val tt3 = parseLocalDate(targetKey)
            if (it3 != null && tt3 != null) {
                if (tt3 > it3) CalendarSlideDirection.LEFT else CalendarSlideDirection.RIGHT
            } else CalendarSlideDirection.NONE
        }
        else -> CalendarSlideDirection.NONE
    }
}

/* Prefix 抽出:
 * "M_2025_10" -> "M"
 * "D_2025-10-08" -> "D"
 * "W_2025-10-06" -> "W"
 * "T3_2025-10-07" -> "T3"
 * "S_2025_10" -> "S"
 */
private fun extractPrefix(key: String): String {
    return when {
        key.startsWith("T3_") -> "T3"
        key.startsWith("M_") -> "M"
        key.startsWith("D_") -> "D"
        key.startsWith("W_") -> "W"
        key.startsWith("S_") -> "S"
        else -> ""
    }
}

private fun parseYearMonth(key: String): YearMonth? {
    // 形式: M_YYYY_M or S_YYYY_M
    // 区切り "_": [prefix, year, month]
    val parts = key.split("_")
    if (parts.size < 3) return null
    val y = parts.getOrNull(1)?.toIntOrNull() ?: return null
    val m = parts.getOrNull(2)?.toIntOrNull() ?: return null
    return runCatching { YearMonth.of(y, m) }.getOrNull()
}

private fun parseLocalDate(key: String): LocalDate? {
    // 形式: D_YYYY-MM-DD / W_YYYY-MM-DD / T3_YYYY-MM-DD
    val idx = key.indexOf('_')
    if (idx < 0 || idx + 1 >= key.length) return null
    val dateStr = key.substring(idx + 1)
    return runCatching { LocalDate.parse(dateStr) }.getOrNull()
}