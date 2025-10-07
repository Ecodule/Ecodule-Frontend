package com.example.ecodule.ui.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable

/**
 * 設定階層用アニメーション。
 * forward = 次の詳細画面へ(右→左)
 * back    = 戻る(左→右)
 */
@Composable
fun SettingsHierarchyAnimatedContent(
    routeKey: String,
    forward: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedContent(
        targetState = routeKey,
        transitionSpec = {
            val enter = if (forward) {
                slideInHorizontally(
                    animationSpec = tween(220),
                    initialOffsetX = { fullWidth -> fullWidth / 3 }
                ) + fadeIn(tween(220))
            } else {
                slideInHorizontally(
                    animationSpec = tween(200),
                    initialOffsetX = { fullWidth -> -fullWidth / 3 }
                ) + fadeIn(tween(200))
            }

            val exit = if (forward) {
                slideOutHorizontally(
                    animationSpec = tween(200, easing = EaseInOut),
                    targetOffsetX = { fullWidth -> -fullWidth / 4 }
                ) + fadeOut(tween(180))
            } else {
                slideOutHorizontally(
                    animationSpec = tween(200, easing = EaseInOut),
                    targetOffsetX = { fullWidth -> fullWidth / 4 }
                ) + fadeOut(tween(180))
            }

            (enter togetherWith exit).using(SizeTransform(clip = false))
        },
        label = "SettingsHierarchyAnimatedContent"
    ) {
        it
    }
}