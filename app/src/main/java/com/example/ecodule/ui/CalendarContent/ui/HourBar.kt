package com.example.ecodule.ui.CalendarContent.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 時間バー（1:00～23:00） */
@Composable
fun HourBar(scrollState: ScrollState? = null) {
    Column(
        modifier = Modifier
            .width(46.dp)
            .fillMaxHeight()
            .verticalScroll(scrollState ?: rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        for (h in 1..23) {
            Box(Modifier.height(48.dp), contentAlignment = Alignment.TopEnd) {
                Text(
                    "${h}:00",
                    color = Color(0xFFBBBBBB),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 2.dp)
                )
            }
        }
    }
}