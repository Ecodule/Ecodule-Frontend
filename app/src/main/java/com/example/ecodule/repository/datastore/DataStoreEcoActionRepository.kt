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
        // ゴミ出し (Garbage)
        EcoAction("garbage_1", EcoActionCategory.GARBAGE, "牛乳パックを資源ごみへ出す", co2Kg = 0.05, savedYen = 1.0),
        EcoAction("garbage_2", EcoActionCategory.GARBAGE, "エコキャップ活動に参加する", co2Kg = 0.05, savedYen = 3.0),
        EcoAction("garbage_3", EcoActionCategory.GARBAGE, "生ごみの水分を絞る", co2Kg = 0.1, savedYen = 1.0),

        // 通勤/通学 (Commute)
        EcoAction("commute_1", EcoActionCategory.COMMUTE, "徒歩や自転車で通う", co2Kg = 0.24, savedYen = 11.0),
        EcoAction("commute_2", EcoActionCategory.COMMUTE, "公共交通機関(バス)で通う", co2Kg = 0.143, savedYen = 0.0),
        EcoAction("commute_3", EcoActionCategory.COMMUTE, "公共交通機関(電車)で通う", co2Kg = 0.2215, savedYen = 0.0),
        EcoAction("commute_4", EcoActionCategory.COMMUTE, "マイボトルを持参する", co2Kg = 0.1, savedYen = 100.0),
        // 補足: 照明(0.03kg, 1.2円)とエアコン(0.41kg, 18.6円)の合計。1時間あたりの数値。
        EcoAction("commute_5", EcoActionCategory.COMMUTE, "家を出る前に照明やエアコンを消す", co2Kg = 0.44, savedYen = 20.0),

        // 買い物 (Shopping)
        EcoAction("shopping_1", EcoActionCategory.SHOPPING, "徒歩や自転車で買い物に行く", co2Kg = 0.24, savedYen = 11.0),
        EcoAction("shopping_2", EcoActionCategory.SHOPPING, "公共交通機関(バス)で買い物に行く", co2Kg = 0.143, savedYen = 0.0),
        EcoAction("shopping_3", EcoActionCategory.SHOPPING, "公共交通機関(電車)で買い物に行く", co2Kg = 0.2215, savedYen = 0.0),
        EcoAction("shopping_4", EcoActionCategory.SHOPPING, "マイバッグを持参する", co2Kg = 0.023, savedYen = 5.0),
        // 補足: 牛肉100gを鶏肉100gに変更した場合
        EcoAction("shopping_5", EcoActionCategory.SHOPPING, "牛肉の代わりに鶏肉を選ぶ", co2Kg = 2.87, savedYen = 240.0),

        // 外出 (Outing)
        EcoAction("outing_1", EcoActionCategory.OUTING, "徒歩や自転車で移動する", co2Kg = 0.24, savedYen = 11.0),
        EcoAction("outing_2", EcoActionCategory.OUTING, "公共交通機関(バス)を利用する", co2Kg = 0.143, savedYen = 0.0),
        EcoAction("outing_3", EcoActionCategory.OUTING, "公共交通機関(電車)を利用する", co2Kg = 0.2215, savedYen = 0.0),
        EcoAction("outing_4", EcoActionCategory.OUTING, "マイボトルを持参する", co2Kg = 0.1, savedYen = 100.0),
        // 補足: 照明(0.03kg, 1.2円)とエアコン(0.41kg, 18.6円)の合計。1時間あたりの数値。
        EcoAction("outing_5", EcoActionCategory.OUTING, "外出前に照明やエアコンを消す", co2Kg = 0.44, savedYen = 20.0),
    )

    override suspend fun getActionsForCategory(category: EcoActionCategory): List<EcoAction> {
        return allEcoActions.filter { it.category == category }
    }

    override suspend fun getAllActions(): List<EcoAction> {
        return allEcoActions
    }
}