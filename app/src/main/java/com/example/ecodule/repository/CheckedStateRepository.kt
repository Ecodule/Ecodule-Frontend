package com.example.ecodule.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * チェック状態（キー: String -> Boolean）を永続化・購読するためのリポジトリ
 * ユーザーごとに保存します。
 */
interface CheckedStateRepository {
    /**
     * 指定ユーザーのチェック状態マップを購読します。
     * アプリ起動時に保存済みの状態を復元し、以降の更新も反映されます。
     */
    fun observeCheckedStates(userId: String): StateFlow<Map<String, Boolean>>

    /**
     * 1件のチェック状態を更新します。
     */
    suspend fun setChecked(userId: String, key: String, checked: Boolean)

    /**
     * すべてのチェック状態を置き換えます（必要に応じて）。
     */
    suspend fun setAll(userId: String, states: Map<String, Boolean>)

    /**
     * 指定ユーザーのチェック状態を全消去します。
     */
    suspend fun clear(userId: String)
}