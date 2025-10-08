package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.R
import java.time.LocalDate

/**
 * 今日に戻るボタン。
 *
 * @param size 枠全体のサイズ
 * @param frameColor 枠(Vector) の色 (tint)
 * @param dayTextColor 日付数字の色
 * @param textOffsetX 日付数字の X 方向オフセット（左に寄せたい場合は負値）
 * @param onClick タップ時コールバック
 */
@Composable
fun TodayReturnButton(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    frameColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
    dayTextColor: Color = MaterialTheme.colorScheme.onSurface,
    textOffsetX: Dp = (-2).dp, // 中央よりやや左へ
    onClick: () -> Unit
) {
    val todayDay = remember { LocalDate.now().dayOfMonth }
    val fontSize = 18.sp
    Surface(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "今日 (${todayDay}日) に戻る" }
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.todayframe),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                colorFilter = ColorFilter.tint(frameColor)
            )
            Text(
                text = todayDay.toString(),
                color = dayTextColor,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .offset(x = textOffsetX)
                // もし縦方向も調整したくなったら:
                // .offset(x = textOffsetX, y = textOffsetY)
            )
        }
    }
}