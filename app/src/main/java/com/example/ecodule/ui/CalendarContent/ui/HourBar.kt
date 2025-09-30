package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 統一パラメータ
val HOUR_HEIGHT_DP = 60.dp
const val HOURS = 24
val HourBarWidth = 56.dp

/**
 * 時間バー（0:00～23:00）
 *
 * - labelNudgeY: マイナスで上に移動。負値のとき自動で先頭に補正スペーサー（-labelNudgeY）を入れて0:00の見切れ回避。
 * - labelNudgeX: プラスで右、マイナスで左に移動。
 * - topPadding: 追加で上側に余白が必要な場合だけ使用（通常は 0.dp のままでOK）。
 */
@Composable
fun HourBar(
    scrollState: ScrollState,
    topPadding: Dp = 0.dp,
    labelNudgeX: Dp = 0.dp,
    labelNudgeY: Dp = 0.dp
) {
    // 0:00見切れ回避のための自動補正
    val autoCompensation = if (labelNudgeY < 0.dp) -labelNudgeY else 0.dp
    val effectiveTopPadding = topPadding + autoCompensation

    Column(
        modifier = Modifier
            .width(HourBarWidth)
            .fillMaxHeight()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top
    ) {
        if (effectiveTopPadding > 0.dp) {
            Spacer(Modifier.height(effectiveTopPadding))
        }
        for (h in 0 until HOURS) {
            Box(
                modifier = Modifier.height(HOUR_HEIGHT_DP),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = "${h}:00",
                    color = Color(0xFFBBBBBB),
                    fontSize = 12.sp,
                    // 右端から少し内側に寄せつつ、XY方向に微調整
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .offset(x = labelNudgeX, y = labelNudgeY)
                )
            }
        }
    }
}