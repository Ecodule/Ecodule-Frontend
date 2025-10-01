package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent

@Composable
fun DayCellGrid(
    day: Int,
    isToday: Boolean,
    events: List<CalendarEvent>,
    onEventClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 日付表示
        Text(
            text = "$day",
            fontSize = 16.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) Color(0xFF88C057) else Color(0xFF444444),
            modifier = Modifier.padding(top = 4.dp)
        )

        // 予定表示（最大3つまで）
        events.take(3).forEach { event ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
                    .clickable { onEventClick(event.id) },
                colors = CardDefaults.cardColors(
                    containerColor = event.color.copy(alpha = 0.8f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text(
                    text = event.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        // 3つ以上の予定がある場合は「+n」を表示
        if (events.size > 3) {
            Text(
                text = "+${events.size - 3}",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}