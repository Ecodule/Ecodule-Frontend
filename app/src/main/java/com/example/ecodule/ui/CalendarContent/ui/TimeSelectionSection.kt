package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun TimeSelectionSection(
    selectedHour: Int,
    selectedMinute: Int,
    onHourSelected: (Int) -> Unit,
    onMinuteSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Hour picker
        WheelPicker(
            items = (0..23).toList(),
            selectedItem = selectedHour,
            onItemSelected = onHourSelected,
            modifier = Modifier.weight(1f),
            label = "時"
        )
        
        // Separator
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ":",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Minute picker
        WheelPicker(
            items = (0..59).toList(),
            selectedItem = selectedMinute,
            onItemSelected = onMinuteSelected,
            modifier = Modifier.weight(1f),
            label = "分"
        )
    }
}

@Composable
private fun WheelPicker(
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    visibleItemsCount: Int = 5
) {
    val itemHeight = 40.dp
    val pickerHeight = itemHeight * visibleItemsCount
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }
    
    // Extended list for circular scrolling effect
    val extendedItems = remember(items) {
        val itemsToAdd = visibleItemsCount * 2
        val startItems = items.takeLast(itemsToAdd)
        val endItems = items.take(itemsToAdd)
        startItems + items + endItems
    }
    
    val listState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    // Calculate the initial scroll position
    val initialScrollIndex = remember(selectedItem, items) {
        val actualIndex = items.indexOf(selectedItem).takeIf { it >= 0 } ?: 0
        actualIndex + visibleItemsCount * 2
    }
    
    // Set initial scroll position
    LaunchedEffect(initialScrollIndex) {
        listState.scrollToItem(initialScrollIndex)
    }
    
    // Track scroll and update selected item
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                val centerIndex = index + if (offset > itemHeightPx / 2) 1 else 0
                val actualIndex = (centerIndex - visibleItemsCount * 2).let { actualIdx ->
                    when {
                        actualIdx < 0 -> {
                            val mod = actualIdx % items.size
                            if (mod == 0) 0 else items.size + mod
                        }
                        actualIdx >= items.size -> actualIdx % items.size
                        else -> actualIdx
                    }
                }
                
                if (actualIndex in items.indices && items[actualIndex] != selectedItem) {
                    onItemSelected(items[actualIndex])
                }
            }
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        Box(
            modifier = Modifier.height(pickerHeight),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                state = listState,
                flingBehavior = snapBehavior,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(extendedItems) { item ->
                    val itemIndex = extendedItems.indexOf(item)
                    val centerIndex by remember {
                        derivedStateOf {
                            listState.firstVisibleItemIndex + 
                            if (listState.firstVisibleItemScrollOffset > itemHeightPx / 2) 1 else 0
                        }
                    }
                    
                    val distanceFromCenter = kotlin.math.abs(itemIndex - centerIndex - 2)
                    val alpha = when (distanceFromCenter) {
                        0 -> 1f
                        1 -> 0.7f
                        else -> 0.3f
                    }
                    
                    val fontSize = when (distanceFromCenter) {
                        0 -> 24.sp
                        1 -> 20.sp
                        else -> 16.sp
                    }
                    
                    val fontWeight = if (distanceFromCenter == 0) FontWeight.Bold else FontWeight.Normal
                    
                    Box(
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth()
                            .alpha(alpha),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%02d", item),
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            color = if (distanceFromCenter == 0) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimeSelectionSectionPreview() {
    MaterialTheme {
        TimeSelectionSection(
            selectedHour = 14,
            selectedMinute = 30,
            onHourSelected = { },
            onMinuteSelected = { },
            modifier = Modifier.padding(16.dp)
        )
    }
}