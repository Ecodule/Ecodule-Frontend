package com.example.ecodule.repository.datastore

import com.example.ecodule.repository.EcoAction
import com.example.ecodule.repository.EcoActionCategory
import com.example.ecodule.repository.EcoActionRepository

import javax.inject.Inject
import javax.inject.Singleton

/**
 * EcoActionの定義リストをメモリ上から提供するリポジトリの実装
 */
@Singleton
class DataStoreEcoActionRepository @Inject constructor(
    private val context: android.content.Context
) : EcoActionRepository {

    // アプリ内で共有されるエコ活の全リスト
    private val allEcoActions: List<EcoAction> = listOf(
        // 買い物
        EcoAction("shopping_1", EcoActionCategory.SHOPPING, "マイバックを持参する", co2Kg = 0.02, savedYen = 5.0),
        EcoAction("shopping_2", EcoActionCategory.SHOPPING, "徒歩や自転車で移動する", co2Kg = 0.24, savedYen = 11.0),
        EcoAction("shopping_3", EcoActionCategory.SHOPPING, "公共交通機関を使う", co2Kg = 0.14, savedYen = 0.0),
        EcoAction("shopping_4", EcoActionCategory.SHOPPING, "牛肉の代わりに鶏肉を買う", co2Kg = 2.87, savedYen = 240.0),
        EcoAction("shopping_5", EcoActionCategory.SHOPPING, "電気やエアコンを消す", co2Kg = 0.44, savedYen = 20.0),
        // 外出
        EcoAction("outing_1", EcoActionCategory.OUTING, "徒歩や自転車で移動する", co2Kg = 0.24, savedYen = 11.0),
        EcoAction("outing_2", EcoActionCategory.OUTING, "公共交通機関を使う", co2Kg = 0.14, savedYen = 0.0),
        EcoAction("outing_3", EcoActionCategory.OUTING, "マイボトルを持参する", co2Kg = 0.1, savedYen = 100.0),
        EcoAction("outing_4", EcoActionCategory.OUTING, "電気やエアコンを消す", co2Kg = 0.44, savedYen = 20.0),
        // ゴミ出し
        EcoAction("garbage_1", EcoActionCategory.GARBAGE, "牛乳パックを資源ごみへ", co2Kg = 0.05, savedYen = 1.0),
        EcoAction("garbage_2", EcoActionCategory.GARBAGE, "生ごみは水分を絞る", co2Kg = 0.1, savedYen = 1.0),
        EcoAction("garbage_3", EcoActionCategory.GARBAGE, "エコキャップ活動への参加", co2Kg = 0.05, savedYen = 3.0),
        // 通勤/通学
        EcoAction("commute_1", EcoActionCategory.COMMUTE, "徒歩や自転車で移動する", co2Kg = 0.24, savedYen = 11.0),
        EcoAction("commute_2", EcoActionCategory.COMMUTE, "公共交通機関を使う", co2Kg = 0.14, savedYen = 0.0),
        EcoAction("commute_3", EcoActionCategory.COMMUTE, "マイボトルを持参する", co2Kg = 0.1, savedYen = 100.0),
        EcoAction("commute_4", EcoActionCategory.COMMUTE, "電気やエアコンを消す", co2Kg = 0.44, savedYen = 20.0),
    )

    override suspend fun getActionsForCategory(category: EcoActionCategory): List<EcoAction> {
        return allEcoActions.filter { it.category == category }
    }

    override suspend fun getAllActions(): List<EcoAction> {
        return allEcoActions
    }
}