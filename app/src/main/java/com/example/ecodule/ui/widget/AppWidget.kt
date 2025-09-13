package com.example.ecodule.ui.widget

import androidx.glance.appwidget.GlanceAppWidget
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.ecodule.MainActivity
import com.example.ecodule.R

class AppWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            MyContent()
        }
    }

    @Composable
    private fun MyContent(modifier : GlanceModifier = GlanceModifier) {
        //Composable形式でレイアウトを記述*Glanceのcomposableを使用すること
        // composeとglanceは一緒に書けないので注意
        Box(
            modifier = modifier
                .clickable(actionStartActivity<MainActivity>())
                .fillMaxSize()
                .background(GlanceTheme.colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = (LocalContext.current.getString(R.string.widget_description)),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,)
            )
        }
    }
}