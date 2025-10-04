package com.example.ecodule.repository

import kotlinx.serialization.Serializable

/**
 * エコ活の種類を定義するEnum
 */
enum class EcoActionCategory(val displayName: String) {
    GARBAGE("ゴミ出し"),
    COMMUTE("通勤/通学"),
    OUTING("外出"),
    SHOPPING("買い物");
}

/**
 * 個々のエコ活の定義を表すデータクラス
 */
@Serializable
data class EcoAction(
    val id: String,
    val category: EcoActionCategory,
    val label: String, // UIに表示する名前 (例: "マイバッグを持参する")
    val co2Kg: Double, // 削減されるCO2量 (kg)
    val savedYen: Double  // 節約される金額 (円)
)