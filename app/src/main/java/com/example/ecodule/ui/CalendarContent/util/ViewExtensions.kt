package com.example.ecodule.ui.CalendarContent.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.composed

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    composed {
        pointerInput(Unit) {
            detectTapGestures(onTap = { onClick() })
        }
    }