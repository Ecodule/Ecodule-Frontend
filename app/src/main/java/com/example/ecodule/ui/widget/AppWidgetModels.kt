package com.example.ecodule.ui.widget

import com.example.ecodule.repository.EcoAction
import com.example.ecodule.repository.EcoActionCategory
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import kotlinx.serialization.Serializable

@Serializable
data class CurrentEventState(
    val id: String,
    val label: String,
    val startHour: Int,
    val endHour: Int,
    val category: String
) {
    companion object {
        fun from(ev: CalendarEvent): CurrentEventState =
            CurrentEventState(
                id = ev.id,
                label = ev.label,
                startHour = ev.startDate.hour,
                endHour = ev.endDate.hour,
                category = ev.category
            )
    }
}

@Serializable
data class EcoActionItem(
    val id: String,
    val label: String,
    val co2Kg: Double,
    val savedYen: Double
)

@Serializable
data class CheckedEntry(
    val key: String,
    val checked: Boolean
)

fun List<EcoAction>.mapToItem(): List<EcoActionItem> =
    this.map { EcoActionItem(id = it.id, label = it.label, co2Kg = it.co2Kg, savedYen = it.savedYen) }

fun mapCategoryToEnum(category: String): EcoActionCategory? =
    when (category) {
        "買い物" -> EcoActionCategory.SHOPPING
        "外出" -> EcoActionCategory.OUTING
        "ゴミ出し" -> EcoActionCategory.GARBAGE
        "通勤/通学" -> EcoActionCategory.COMMUTE
        else -> null
    }

fun buildCheckedKey(event: CurrentEventState, item: EcoActionItem): String =
    "${event.label}-${item.label}-${event.startHour}"