package com.example.ecodule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * カテゴリータブを表示するComposable
 */
@Composable
fun CategoryTabs(
    categories: List<Pair<String, Color>>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        categories.forEach { (cat, color) ->
            val isSelected = selectedCategory == cat
            Text(
                text = cat,
                color = Color.Black,
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(
                        if (isSelected) color else Color(0xFFF4F4F4),
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onCategorySelected(cat) }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
