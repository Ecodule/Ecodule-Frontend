package com.example.ecodule.ui.addtaskcontent.section

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun DeleteConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("予定を削除") },
        text = { Text("この予定を削除しますか？") },
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
                Text("削除", color = Color.Companion.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}