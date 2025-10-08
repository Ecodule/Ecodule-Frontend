package com.example.ecodule.ui.addtaskcontent.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ActionButtons(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean,
    saveLabel: String
) {
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(onClick = onCancel) {
            Text("キャンセル")
        }
        Button(
            onClick = onSave,
            enabled = canSave
        ) {
            Text(saveLabel)
        }
    }
}