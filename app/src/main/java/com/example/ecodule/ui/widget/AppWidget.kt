package com.example.ecodule.ui.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.ecodule.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.format.TextStyle as JTextStyle
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class AppWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        ensureInitialized(context, id)

        provideContent {
            GlanceTheme {
                Content(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ImageProvider(R.drawable.ecodule_wiget))
                        .cornerRadius(16.dp)
                        .padding(12.dp)
                )
            }
        }
    }

    @Composable
    private fun Content(modifier: GlanceModifier) {
        val prefs: Preferences = currentState()

        val currentEvent: CurrentEventState? = prefs[PrefKeys.currentEventJson]?.let {
            runCatching { Json.decodeFromString(CurrentEventState.serializer(), it) }.getOrNull()
        }

        val ecoActions: List<EcoActionItem> = prefs[PrefKeys.ecoActionsJson]?.let {
            runCatching { Json.decodeFromString(ListSerializer(EcoActionItem.serializer()), it) }.getOrElse { emptyList() }
        } ?: emptyList()

        val checkedMap: Map<String, Boolean> = prefs[PrefKeys.checkedJson]?.let {
            runCatching {
                val list = Json.decodeFromString(ListSerializer(CheckedEntry.serializer()), it)
                list.associate { entry -> entry.key to entry.checked }
            }.getOrElse { emptyMap() }
        } ?: emptyMap()

        Log.d("AppWidget", "Rendering widget with event:, checkedMap: $checkedMap")

        val black = ColorProvider(day = Color.Black, night = Color.Black)

        val today = LocalDate.now()
        val weekDayJa = today.dayOfWeek.getDisplayName(JTextStyle.SHORT, Locale.JAPANESE)
        val dateStr = today.format(DateTimeFormatter.ofPattern("M/d"))

        Column(modifier) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${weekDayJa}曜日",
                        style = TextStyle(color = black, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = dateStr,
                        style = TextStyle(
                            color = black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
                Spacer(modifier = GlanceModifier.width(8.dp))
                if (currentEvent == null) {
                    Text(
                        text = "本日の予定はありません".truncate(11),
                        style = TextStyle(color = black, fontWeight = FontWeight.Bold)
                    )
                } else {
                    Column {
                        Text(
                            text = currentEvent.label.truncate(11),
                            style = TextStyle(
                                color = black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        )
                        Text(
                            text = "${currentEvent.startHour.toString().padStart(2, '0')}:${currentEvent.startMin.toString().padStart(2, '0')} ~ ${currentEvent.endHour.toString().padStart(2, '0')}:${currentEvent.endMin.toString().padStart(2, '0')}"
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                Button(
                    text = "完了",
                    onClick = actionRunCallback<NextEventAction>(),
                )
            }

            Spacer(GlanceModifier.height(4.dp))

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .background(ColorProvider(Color.White.copy(alpha = 0.5f)))
                    .cornerRadius(8.dp)
                    .padding(2.dp)
            ) {
                if (currentEvent == null) {
                    Text(
                        text = "表示できる予定がありません",
                        style = TextStyle(color = black)
                    )
                } else {
                    LazyColumn {
                        items(ecoActions) { item ->
                            val key = buildCheckedKey(currentEvent, item)
                            EcoActionRow(
                                ecoAction = item,
                                checked = checkedMap[key] ?: false,
                                textColor = black
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun EcoActionRow(
        ecoAction: EcoActionItem,
        checked: Boolean,
        textColor: ColorProvider
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CheckBox(
                checked = checked,
                onCheckedChange = actionRunCallback<ToggleEcoActionAction>(
                    parameters = actionParametersOf(
                        PrefKeys.ecoActionIdParam to ecoAction.id
                    )
                ),
                modifier = GlanceModifier.padding(end = 8.dp)
            )
            Text(
                text = "${ecoAction.label}\n ¥${ecoAction.savedYen} / CO₂: ${ecoAction.co2Kg}kg",
                style = TextStyle(color = textColor, fontSize = 16.sp),
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }
}

fun String.truncate(maxLength: Int): String {
    if (this.length <= maxLength) {
        return this
    }
    return this.substring(0, maxLength) + "…" // … (三点リーダー)
}