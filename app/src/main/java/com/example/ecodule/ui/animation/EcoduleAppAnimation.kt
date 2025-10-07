package com.example.ecodule.ui.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.example.ecodule.ui.EcoduleRoute

/**
 * 画面遷移アニメーション制御コンテナ。
 *
 * 仕様:
 *  - BottomNav トップレベル同士: フェードのみ (左右移動なし)
 *  - Forward (階層的に進む):
 *      Enter: 右→左へスライド + フェードイン (300ms)
 *      Exit : フェードアウトのみ（スライドなし）
 *  - Back (階層的に戻る):
 *      Enter: フェードインのみ（スライドなし）
 *      Exit : 左→右へスライド + フェードアウト (300ms)
 *
 * 戻り判定:
 *   各 route の初出順序 index を記録し、target の index < initial の index なら Back とみなす。
 *   （TopLevel 同士は常にフェードのみで上記ロジックを上書き）
 */
@Composable
fun EcoduleAnimatedNavContainer(
    currentRoute: String,
    modifier: Modifier = Modifier,
    content: @Composable (String) -> Unit
) {
    val topLevelRoutes = remember {
        setOf(
            EcoduleRoute.CALENDAR,
            EcoduleRoute.TASKSLIST,
            EcoduleRoute.STATISTICS,
            EcoduleRoute.SETTINGS
        )
    }

    // Route 初回訪問順インデックス
    val orderMap = remember { mutableMapOf<String, Int>() }
    val counter = remember { mutableIntStateOf(0) }
    // 最新 currentRoute を記録（transitionSpec 内で参照されるため updatedState 化）
    val currentRouteState = rememberUpdatedState(currentRoute)

    // 初めて来た route なら順序を割り振る
    if (!orderMap.containsKey(currentRouteState.value)) {
        orderMap[currentRouteState.value] = counter.intValue
        counter.intValue = counter.intValue + 1
    }

    val duration = 300

    AnimatedContent(
        targetState = currentRoute,
        modifier = modifier,
        transitionSpec = {
            val initialIdx = orderMap[initialState] ?: Int.MAX_VALUE
            val targetIdx = orderMap[targetState] ?: Int.MAX_VALUE

            val isTopToTop = initialState in topLevelRoutes && targetState in topLevelRoutes
            val isBack = !isTopToTop && targetIdx < initialIdx

            if (isTopToTop) {
                // TopLevel 間はフェードのみ
                fadeIn(animationSpec = tween(duration)) togetherWith
                        fadeOut(animationSpec = tween(duration))
            } else {
                if (isBack) {
                    // Back: 新しい画面はフェードインのみ / 古い画面は右方向へスライドしながらフェードアウト
                    val enter = fadeIn(animationSpec = tween(duration))
                    val exit = fadeOut(animationSpec = tween(duration)) +
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(duration)
                            )
                    (enter togetherWith exit)
                } else {
                    // Forward: 新しい画面は右→左スライド + フェードイン / 古い画面はフェードアウトのみ
                    val enter = fadeIn(animationSpec = tween(duration)) +
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(duration)
                            )
                    val exit = fadeOut(animationSpec = tween(duration))
                    (enter togetherWith exit)
                }
            }.using(SizeTransform(clip = false))
        },
        label = "AppNavigationAnimatedContent"
    ) { route ->
        content(route)
    }
}