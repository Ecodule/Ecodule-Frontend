package com.example.ecodule.ui.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * アプリ全体の画面遷移アニメーション定義。
 *
 * 要件:
 *  - 入場: 新しい画面が右から左へ slide + 同時に fadeIn (300ms)
 *  - 退場: 現在の画面が左から右へ slide + fadeOut (300ms)
 *
 * すべての遷移で同一パターンを適用。
 * forward/back や TopLevel の特別扱いは廃止。
 */
@Composable
fun EcoduleAnimatedNavContainer(
    currentRoute: String,
    modifier: Modifier = Modifier,
    content: @Composable (String) -> Unit
) {
    AnimatedContent(
        targetState = currentRoute,
        modifier = modifier,
        transitionSpec = {
            val duration = 300
            val enter = fadeIn(animationSpec = tween(duration)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(duration)
                    )
            val exit = fadeOut(animationSpec = tween(duration)) +
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(duration)
                    )
            (enter togetherWith exit).using(SizeTransform(clip = false))
        },
        label = "AppNavigationAnimatedContent"
    ) { route ->
        content(route)
    }
}

