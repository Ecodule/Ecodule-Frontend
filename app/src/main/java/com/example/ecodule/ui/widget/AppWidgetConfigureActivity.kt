package com.example.ecodule.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.ecodule.R

class AppWidgetConfigureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ウィジェットを追加する前に、既存のインスタンスがないかチェック
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val provider = android.content.ComponentName(this, MyWidgetReceiver::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(provider)

        // 既存のウィジェットが1つ以上ある場合
        if (appWidgetIds.size > 1) {
            // ユーザーにメッセージを表示してActivityを終了
            Toast.makeText(this, "このウィジェットは1つしか設置できません。", Toast.LENGTH_LONG).show()

            // 今回追加しようとしたウィジェットは不要なので結果をキャンセルして終了
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        // ウィジェットが1つも存在しない場合（今回が初めての追加）
        // ウィジェットIDを取得
        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // ウィジェットの追加を確定
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}