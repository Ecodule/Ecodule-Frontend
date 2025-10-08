package com.example.ecodule.ui.addtaskcontent.section

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MemoSection(
    memo: String,
    onMemoChange: (String) -> Unit
) {
    OutlinedTextField(
        value = memo,
        onValueChange = onMemoChange,
        label = { Text("メモ") },
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(160.dp)
            .padding(vertical = 8.dp)
    )
}