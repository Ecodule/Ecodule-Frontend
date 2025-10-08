package com.example.ecodule.ui.addtaskcontent.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RepeatSection(
    repeatOption: String,
    onRepeatOptionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        Text("繰り返し", modifier = Modifier.Companion.weight(1f))
        Box {
            Button(onClick = { expanded = true }) {
                Text(repeatOption)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("しない", "毎日", "毎週", "毎月", "毎年").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onRepeatOptionChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}