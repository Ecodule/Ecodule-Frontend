package com.example.ecodule.ui.taskListContent

/**
 * タスクリスト画面向け：タスク説明文の単一ソース。
 *
 * - まず安定した taskId（ドメインID）があれば ID で解決
 * - ID が無い UI レイヤでは title でフォールバック解決
 * - 必ずこのファイル経由で説明文を取得してください
 */
object TaskListInformation {

    // 可能ならドメイン層と共有する安定 ID を利用してください
    object Code {
        const val SeparateWaste = "separate_waste"
        const val UseCompost = "use_compost"
        const val WalkOrBike = "walk_or_bike"
        const val UsePublicTransport = "use_public_transport"
        const val BringMyBottle = "bring_my_bottle"
    }

    // ID ベースの説明
    private val byId: Map<String, String> = mapOf(
        Code.SeparateWaste to "家庭の可燃・不燃・資源ごみを正しく分別して出します。分別はリサイクル効率を高め、焼却・埋立の負担を減らします。",
        Code.UseCompost to "生ごみは家庭用コンポストで堆肥化します。ごみ量と焼却に伴う CO₂ 排出を抑制できます。",
        Code.WalkOrBike to "近距離の移動は徒歩や自転車を選びます。健康増進と同時に交通由来の排出を削減します。",
        Code.UsePublicTransport to "可能な移動は公共交通機関を利用します。自家用車の利用頻度を下げ、排出を抑制します。",
        Code.BringMyBottle to "外出時はマイボトルを持参し、使い捨て容器の利用を減らします。資源消費と廃棄を抑えます。"
    )

    // タイトルによるフォールバック（UI に安定 ID が無い場合）
    // タイトル文言を変更する場合は、必ずこちらも更新してください
    private val byTitle: Map<String, String> = mapOf(
        "ゴミを分別する" to byId.getValue(Code.SeparateWaste),
        "生ゴミはコンポスト利用" to byId.getValue(Code.UseCompost),
        "徒歩や自転車で通う" to byId.getValue(Code.WalkOrBike),
        "公共交通機関を使う" to byId.getValue(Code.UsePublicTransport),
        "マイボトルを持参する" to byId.getValue(Code.BringMyBottle)
    )

    /**
     * UI からの取得口。ID があれば ID 優先、無ければタイトルで解決。
     */
    fun descriptionFor(taskId: String?, title: String): String {
        return taskId?.let { byId[it] } ?: byTitle[title] ?: "このタスクの説明は準備中です。"
    }
}