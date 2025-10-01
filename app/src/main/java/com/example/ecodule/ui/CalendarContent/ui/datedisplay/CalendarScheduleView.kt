package com.example.ecodule.ui.CalendarContent.ui.datedisplay

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.CalendarContent.util.noRippleClickable
import java.time.YearMonth

@Composable
fun CalendarScheduleView(
    yearMonth: YearMonth,
    events: List<CalendarEvent>,
    onDayClick: (Int) -> Unit = {},
    onEventClick: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(events) { event ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .noRippleClickable { onDayClick(event.startDate.dayOfMonth) }
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 日付表示
                Column(
                    modifier = Modifier.width(60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${event.startDate.dayOfMonth}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF444444)
                    )
                    Text(
                        text = "${event.startDate.monthValue}月",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 予定カード
                Card(
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(
                        containerColor = event.color,
                        contentColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEventClick(event.id) }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        // 予定タイトル
                        Text(
                            text = event.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // 時間表示
                        if (event.startDate.hour != null && event.endDate.hour != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${event.startDate.hour}:00 - ${event.endDate.hour}:00",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        } else if (event.allDay) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "終日",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        // カテゴリ表示
                        if (event.category.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📂 ${event.category}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        // メモ表示（短縮版）
                        if (event.memo.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = event.memo.take(50) + if (event.memo.length > 50) "..." else "",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // 区切り線
            if (events.indexOf(event) < events.size - 1) {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray.copy(alpha = 0.3f)
                )
            }
        }

        // 予定がない場合の表示
        if (events.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📅",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${yearMonth.year}年${yearMonth.monthValue}月の予定はありません",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "右下の + ボタンから予定を追加できます",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}