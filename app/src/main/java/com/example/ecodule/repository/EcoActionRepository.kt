package com.example.ecodule.repository

/**
 * エコ活の定義リストを提供するリポジトリのインターフェース
 */
interface EcoActionRepository {

    /**
     * 指定されたカテゴリに属するすべてのエコ活の定義リストを取得します。
     *
     * @param category 取得したいエコ活のカテゴリ
     * @return EcoActionのリスト
     */
    suspend fun getActionsForCategory(category: EcoActionCategory): List<EcoAction>

    /**
     * すべてのカテゴリのエコ活定義リストを取得します。
     *
     * @return EcoActionのリスト
     */
    suspend fun getAllActions(): List<EcoAction>
}