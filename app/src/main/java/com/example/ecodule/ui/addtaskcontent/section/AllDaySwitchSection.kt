package com.example.ecodule.ui.addtaskcontent.section

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AllDaySwitch(
    allDay: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        Text("終日", modifier = Modifier.Companion.weight(1f))
        Switch(
            checked = allDay,
            onCheckedChange = onChange
        )
    }
}