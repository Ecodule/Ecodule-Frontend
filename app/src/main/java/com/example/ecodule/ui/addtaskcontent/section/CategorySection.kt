package com.example.ecodule.ui.addtaskcontent.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ecodule.ui.components.CategoryTabs

@Composable
fun CategorySection(
    categories: List<Pair<String, Color>>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    CategoryTabs(
        categories = categories,
        selectedCategory = selectedCategory,
        onCategorySelected = onCategorySelected
    )
}