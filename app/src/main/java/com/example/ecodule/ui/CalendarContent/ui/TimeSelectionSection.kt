package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun TimeSelectionSection(
    initialHour: Int,
    initialMinute: Int,
    onHourSelected: (String) -> Unit,
    onMinuteSelected: (String) -> Unit,
    // ここで時・分それぞれの中心位置バイアスを指定できます（+で上、-で下）
    hourCenterBiasDp: Dp = 0.dp,
    minuteCenterBiasDp: Dp = 0.dp,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 時間（00〜23）
        val hours = (0..23).map { "%02d".format(it) }
        val hourIndex = hours.indexOf("%02d".format(initialHour)).let { if (it >= 0) it else 0 }

        InfiniteItemsPicker(
            items = hours,
            initialIndexInItems = hourIndex,
            onItemSelected = onHourSelected,
            modifier = Modifier.weight(1f),
            centerBiasDp = hourCenterBiasDp
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .width(16.dp),
            textAlign = TextAlign.Center
        )

        // 分（5分刻み：00,05,10,...,55）
        val minutes = (0..59 step 5).map { "%02d".format(it) }
        val roundedInitialMinute = (initialMinute / 5) * 5
        val minuteIndex = minutes.indexOf("%02d".format(roundedInitialMinute)).let { if (it >= 0) it else 0 }

        InfiniteItemsPicker(
            items = minutes,
            initialIndexInItems = minuteIndex,
            onItemSelected = onMinuteSelected,
            modifier = Modifier.weight(1f),
            centerBiasDp = minuteCenterBiasDp
        )
    }
}

/**
 * 無限スクロール風ホイール。
 * - items を大きく繰り返して表示
 * - 停止時と centerBias 変更時に「目標オフセット = centerBiasDp」にスナップ
 * - 端に近づいたら中央ブロックへ戻すときも同じオフセットを維持
 */
@Composable
private fun InfiniteItemsPicker(
    items: List<String>,
    initialIndexInItems: Int,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    centerBiasDp: Dp = 0.dp, // +で選択行が少し上に、-で少し下に見える
) {
    val repeatCount = 1_000
    val totalCount = items.size * repeatCount
    val middleBlockStart = (repeatCount / 2) * items.size

    // 表示寸法（アイテム本体 + アイテム間余白）— Text の行高に合わせて必要なら調整
    val itemHeight = 40.dp
    val itemSpacing = 16.dp
    val density = LocalDensity.current
    val perItemPx = with(density) { (itemHeight + itemSpacing).toPx() }

    // 目標スクロールオフセット（0..perItemPx の範囲に正規化）
    val rawBiasPx = with(density) { centerBiasDp.toPx() }
    val targetOffsetPx = ((rawBiasPx % perItemPx) + perItemPx) % perItemPx

    // 初期配置：中央ブロックの desiredIndex を中央行に置くため firstVisible = desiredIndex - 1、
    // かつ firstVisibleItemScrollOffset に targetOffsetPx をセット
    val desiredIndex = middleBlockStart + (initialIndexInItems % items.size)
    val initialFirstVisible = desiredIndex - 1
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialFirstVisible,
        initialFirstVisibleItemScrollOffset = targetOffsetPx.toInt()
    )

    // スクロール停止時に「目標オフセット」へ最短で合わせる
    // 1) 最寄りのアイテム境界へ微調整して中央にピタッと合わせる
    // 2) 選択値を通知
    // 3) 端に近づいていたら中央ブロックへ戻す
    LaunchedEffect(listState.isScrollInProgress, targetOffsetPx) {
        if (!listState.isScrollInProgress) {
            val current = listState.firstVisibleItemScrollOffset.toFloat()
            var adjust = targetOffsetPx - current
            // 最短距離に正規化（-perItem/2 .. +perItem/2）
            if (adjust > perItemPx / 2f) adjust -= perItemPx
            if (adjust < -perItemPx / 2f) adjust += perItemPx

            if (abs(adjust) > 0.5f) {
                listState.animateScrollBy(adjust)
            }

            // 揃えた後の中央行（選択中の値）
            val centerIndex = listState.firstVisibleItemIndex + 1
            val value = items[(centerIndex % items.size + items.size) % items.size]
            onItemSelected(value)

            // 端に近づいたら中央ブロックへ戻す（同じ targetOffsetPx を維持）
            val threshold = items.size
            if (centerIndex < threshold || centerIndex > totalCount - threshold) {
                val normalized = ((centerIndex % items.size) + items.size) % items.size
                val newCenter = middleBlockStart + normalized
                listState.scrollToItem(newCenter - 1, targetOffsetPx.toInt())
            }
        }
    }

    Box(
        modifier = modifier.height(itemHeight * 3 + itemSpacing * 2),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(totalCount) { index ->
                val value = items[(index % items.size + items.size) % items.size]

                // 中央行（選択中）
                val selectedIndex by remember {
                    derivedStateOf { listState.firstVisibleItemIndex + 1 }
                }
                val isSelected by remember {
                    derivedStateOf { index == selectedIndex }
                }

                val alpha = if (isSelected) 1f else 0.3f
                val fontSize = if (isSelected) 24.sp else 16.sp
                val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

                Text(
                    text = value,
                    modifier = Modifier
                        .height(itemHeight)
                        .alpha(alpha),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSize,
                        fontWeight = fontWeight
                    ),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(itemSpacing))
            }
        }
    }
}