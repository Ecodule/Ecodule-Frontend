package com.example.ecodule.ui.taskListContent.component

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.ecodule.repository.EcoAction
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import com.example.ecodule.ui.taskListContent.darkenColor
import com.example.ecodule.ui.taskListContent.model.TaskListViewModel
import kotlin.collections.forEach

@Composable
fun TaskSectionWithTitleAndTime(
    event: CalendarEvent,
    items: List<EcoAction>,
    checkedStates: Map<String, Boolean>,
    expanded: Boolean,
    backgroundColor: Color,
    onCheckedChange: (String, Boolean) -> Unit,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("TaskSection", "Rendering section for event: ${event.label}, ecoActions: ${items.map { it.label }}")

    Card(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clickable { onExpandToggle() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = event.label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (event.startDate.hour != null && event.endDate.hour != null) {
                Text(
                    text = "${event.startDate.hour}:00 ～ ${event.endDate.hour}:00",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(0.dp))

            // タップで展開
            if (expanded) {
                val checkboxColor = darkenColor(backgroundColor, 0.6f)
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    items.forEach { ecoAction ->
                        val key = "${event.label}-${ecoAction.label}-${event.startDate.hour}"
                        val checked = checkedStates[key] ?: false
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { onCheckedChange(ecoAction.label, it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = checkboxColor,
                                    uncheckedColor = checkboxColor,
                                    checkmarkColor = Color.White
                                )
                            )
                            Column {
                                Text(
                                    text = ecoAction.label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "CO₂削減量: ${ecoAction.co2Kg}kg / 節約額: ¥${ecoAction.savedYen}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            } else {
                // 展開前のヒント
                Text(
                    text = "タップして詳細を表示",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}