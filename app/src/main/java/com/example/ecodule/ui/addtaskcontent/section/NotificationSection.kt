package com.example.ecodule.ui.addtaskcontent.section

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotificationSection(
    notificationMinutes: Int,
    onNotificationChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("通知", modifier = Modifier.weight(1f))
        Slider(
            value = notificationMinutes.toFloat(),
            onValueChange = { onNotificationChange(it.toInt()) },
            valueRange = 0f..60f,
            steps = 5,
            modifier = Modifier.weight(2f)
        )
        Text("${notificationMinutes}分前")
    }
}