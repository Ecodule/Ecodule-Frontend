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
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

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
            val dir = targetState.direction

            val enter = when (dir) {
                CalendarSlideDirection.LEFT -> {
                    // 次へ: 新ページは右から
                    slideInHorizontally(
                        animationSpec = tween(duration, easing = FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> fullWidth / 2 }
                    ) + fadeIn(tween(duration))
                }
                CalendarSlideDirection.RIGHT -> {
                    // 戻る: 新ページは左から
                    slideInHorizontally(
                        animationSpec = tween(duration, easing = FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> -fullWidth / 2 }
                    ) + fadeIn(tween(duration))
                }
                CalendarSlideDirection.NONE -> {
                    fadeIn(tween(150))
                }
            }

            val exit = when (dir) {
                CalendarSlideDirection.LEFT -> {
                    // 旧ページは左へ
                    slideOutHorizontally(
                        animationSpec = tween(duration),
                        targetOffsetX = { fullWidth -> -fullWidth / 2 }
                    ) + fadeOut(tween(duration - 40))
                }
                CalendarSlideDirection.RIGHT -> {
                    // 旧ページは右へ
                    slideOutHorizontally(
                        animationSpec = tween(duration),
                        targetOffsetX = { fullWidth -> fullWidth / 2 }
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