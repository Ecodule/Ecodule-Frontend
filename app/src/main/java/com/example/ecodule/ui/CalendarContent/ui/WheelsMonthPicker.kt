package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.min

/* ---------------- Internal model ---------------- */
private sealed class WheelItem {
    data class YearHeader(val year: Int) : WheelItem()
    data class Month(val ym: YearMonth) : WheelItem()
}

@Composable
fun WheelsMonthPicker(
    currentMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,

    // Visual
    barHeight: Dp = 44.dp,
    monthItemWidth: Dp = 60.dp,
    selectedFontSizeSp: Int = 24,
    unselectedFontSizeSp: Int = 16,

    // Anchor (committed month head from left)
    snapAlignOffsetDp: Dp = 0.dp,

    // Padding
    contentStartPaddingDp: Dp = 8.dp,
    contentEndPaddingDp: Dp = 12.dp,

    // Behavior tuning
    resnapTolerancePx: Int = 2,
    idleExtraFrames: Int = 2,           // idle 検知後さらに安定化のため待つフレーム数
    previewFrameInterval: Int = 1,      // スクロール中プレビュー更新間隔 (1 = 毎フレーム)
    debugLog: Boolean = false
) {
    /* ---------- Build item list ---------- */
    val anchorBase = remember { YearMonth.of(2000, 1) }
    val totalMonths = 4800
    val center = totalMonths / 2
    val minYm = remember { anchorBase.minusMonths(center.toLong()) }
    val maxYm = remember { anchorBase.plusMonths((totalMonths - 1L - center).toLong()) }
    val minYear = minYm.year
    val maxYear = maxYm.year

    data class ItemsBundle(val items: List<WheelItem>, val indexMap: Map<YearMonth, Int>)
    val bundle = remember {
        val list = ArrayList<WheelItem>(totalMonths + (maxYear - minYear + 1))
        val map = HashMap<YearMonth, Int>(totalMonths)
        for (y in minYear..maxYear) {
            list.add(WheelItem.YearHeader(y))
            for (m in 1..12) {
                val ym = YearMonth.of(y, m)
                if (ym.isBefore(minYm) || ym.isAfter(maxYm)) continue
                map[ym] = list.size
                list.add(WheelItem.Month(ym))
            }
        }
        ItemsBundle(list, map)
    }
    val items = bundle.items
    val monthIndexMap = bundle.indexMap

    /* ---------- Scroll state ---------- */
    val initialIndex = remember(currentMonth) { monthIndexMap[currentMonth] ?: 0 }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val scope = rememberCoroutineScope()

    // Preview & commit
    var previewMonth by remember { mutableStateOf<YearMonth?>(currentMonth) }
    var lastCommittedMonth by rememberSaveable { mutableStateOf(currentMonth) }

    val density = LocalDensity.current
    val snapAlignOffsetPx = with(density) { snapAlignOffsetDp.toPx() }
    val startPaddingPx = with(density) { contentStartPaddingDp.toPx() }

    val interactionSource = remember { MutableInteractionSource() }

    /* ---------- Helpers ---------- */
    fun monthAt(i: Int): YearMonth? = (items.getOrNull(i) as? WheelItem.Month)?.ym

    fun closestMonthIndex(): Int? {
        val layout = listState.layoutInfo
        if (layout.visibleItemsInfo.isEmpty()) return null
        val anchorX = layout.viewportStartOffset + startPaddingPx + snapAlignOffsetPx
        var bestIdx: Int? = null
        var bestDist = Float.MAX_VALUE

        fun consider(idx: Int, refOffset: Int) {
            val d = abs(refOffset - anchorX)
            if (d < bestDist) {
                bestDist = d
                bestIdx = idx
            }
        }
        for (info in layout.visibleItemsInfo) {
            when (val it = items.getOrNull(info.index)) {
                is WheelItem.Month -> consider(info.index, info.offset)
                is WheelItem.YearHeader -> {
                    val nxt = info.index + 1
                    if (items.getOrNull(nxt) is WheelItem.Month) {
                        consider(nxt, info.offset)
                    }
                }
                else -> {}
            }
        }
        return bestIdx
    }

    suspend fun snapTo(index: Int) {
        val desiredOffset = (startPaddingPx + snapAlignOffsetPx).toInt().coerceAtLeast(0)
        val need = listState.firstVisibleItemIndex != index ||
                abs(listState.firstVisibleItemScrollOffset - desiredOffset) > resnapTolerancePx
        if (!need) return
        val distance = abs(listState.firstVisibleItemIndex - index)
        try {
            if (distance > 36) {
                listState.scrollToItem(index, scrollOffset = desiredOffset)
            } else {
                listState.animateScrollToItem(index, scrollOffset = desiredOffset)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Throwable) {
            if (debugLog) println("snapTo exception: $e")
        }
    }

    suspend fun commitNearest(reason: String) {
        try {
            // 追加フレーム待ちで layout 安定
            repeat(idleExtraFrames) { withFrameNanos { } }
            val idx = closestMonthIndex() ?: return
            val ym = monthAt(idx) ?: return
            snapTo(idx)
            if (ym != lastCommittedMonth) {
                lastCommittedMonth = ym
                if (ym != currentMonth) onMonthChanged(ym)
                if (debugLog) println("Commit [$reason] -> $ym")
            }
            previewMonth = ym
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Throwable) {
            if (debugLog) println("commitNearest exception: $e")
        }
    }

    fun forcePreviewUpdate(reason: String) {
        try {
            val idx = closestMonthIndex()
            val ym = idx?.let { monthAt(it) }
            if (ym != null && ym != lastCommittedMonth) {
                previewMonth = ym
            } else if (ym != null && previewMonth != ym) {
                // commit済みと同じ → プレビューも揃えておく
                previewMonth = ym
            }
            if (debugLog && ym != null) {
                // println("Preview [$reason] -> $ym")
            }
        } catch (e: Throwable) {
            if (debugLog) println("forcePreviewUpdate exception: $e")
        }
    }

    /* ---------- Parent month change ---------- */
    LaunchedEffect(currentMonth) {
        val target = monthIndexMap[currentMonth] ?: return@LaunchedEffect
        snapTo(target)
        lastCommittedMonth = currentMonth
        previewMonth = currentMonth
    }

    /* ---------- Scroll-stop commit loop (await idle) ---------- */
    LaunchedEffect(Unit) {
        while (isActive) {
            // 待機: スクロールが完全停止するまで
            snapshotFlow { listState.isScrollInProgress }
                .collect { scrolling ->
                    if (!scrolling) {
                        // 一度停止検知したら commit
                        commitNearest("idle-detect")
                        // break out to outer loop restart (再度監視)
                        return@collect
                    }
                }
        }
    }

    /* ---------- Continuous preview while scrolling (frame loop) ---------- */
    LaunchedEffect(Unit) {
        // フレーム駆動プレビュー
        while (isActive) {
            if (listState.isScrollInProgress) {
                forcePreviewUpdate("frame")
                // 間引き
                var frames = 0
                while (frames < previewFrameInterval && listState.isScrollInProgress) {
                    withFrameNanos { }
                    frames++
                }
            } else {
                // 静止時は少し待ってループ継続
                withFrameNanos { }
            }
        }
    }

    /* ---------- InteractionSource：指離し（ドラッグ終端）で早期コミット予約 ---------- */
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { inter ->
            if (inter is DragInteraction.Stop || inter is DragInteraction.Cancel) {
                // 指を離した時点でまだ慣性中でもプレビューを確定方向へ近づける
                forcePreviewUpdate("drag-stop")
                // 一拍後に（慣性がすぐ終わったケース用）commit 試行
                scope.launch {
                    delay(40)
                    if (!listState.isScrollInProgress) {
                        commitNearest("drag-stop-fast")
                    }
                }
            }
        }
    }

    /* ---------- Click selection (immediate) ---------- */
    fun clickSelect(ym: YearMonth) {
        val idx = monthIndexMap[ym] ?: return
        scope.launch {
            snapTo(idx)
            if (ym != lastCommittedMonth) {
                lastCommittedMonth = ym
                if (ym != currentMonth) onMonthChanged(ym)
            }
            previewMonth = ym
        }
    }

    /* ---------- UI ---------- */
    LazyRow(
        modifier = modifier
            .height(barHeight)
            // InteractionSource を設定してドラッグ終端検知
            .scrollable(
                orientation = Orientation.Horizontal,
                state = rememberScrollableState { delta -> -delta /* consume 0 (delegated) */ },
                interactionSource = interactionSource,
                enabled = false // 実際のスクロール処理は LazyRow 自身に任せる (interaction だけ得る)
            ),
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(
            start = contentStartPaddingDp,
            end = contentEndPaddingDp
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(
            items = items,
            key = { i, it ->
                when (it) {
                    is WheelItem.YearHeader -> "y-${it.year}-$i"
                    is WheelItem.Month -> "m-${it.ym.year}-${it.ym.monthValue}"
                }
            }
        ) { _, it ->
            when (it) {
                is WheelItem.YearHeader -> YearHeaderChip(it.year)
                is WheelItem.Month -> {
                    val ym = it.ym
                    val selected = ym == lastCommittedMonth
                    val preview = !selected && ym == previewMonth
                    MonthChip(
                        label = "${ym.month.value}月",
                        selected = selected,
                        preview = preview,
                        onClick = { if (!selected) clickSelect(ym) },
                        width = monthItemWidth,
                        barHeight = barHeight,
                        selectedFontSizeSp = selectedFontSizeSp,
                        unselectedFontSizeSp = unselectedFontSizeSp
                    )
                }
            }
        }
    }
}

/* ---------------- Visual components ---------------- */
@Composable
private fun MonthChip(
    label: String,
    selected: Boolean,
    preview: Boolean,
    onClick: () -> Unit,
    width: Dp,
    barHeight: Dp,
    selectedFontSizeSp: Int,
    unselectedFontSizeSp: Int
) {
    val active = selected || preview
    val color = if (selected) Color.Black else Color(0xFF8C8C8C)
    val fontSize = if (active) selectedFontSizeSp.sp else unselectedFontSizeSp.sp
    val weight = if (active) FontWeight.SemiBold else FontWeight.Medium

    Row(
        modifier = Modifier
            .height(barHeight)
            .width(width)
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = weight,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
private fun YearHeaderChip(year: Int) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = Modifier
            .height(24.dp)
            .clip(shape)
            .border(0.8.dp, Color(0xFFBDBDBD), shape)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = year.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575),
            maxLines = 1
        )
    }
}