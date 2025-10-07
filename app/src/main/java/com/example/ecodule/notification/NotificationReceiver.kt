package com.example.ecodule.notification

import android.Manifest
import android.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class NotificationReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        // 通知チャネルを必ず作成（O+）
        createNotificationChannel(context)

        val eventId = intent.getStringExtra("event_id") ?: return
        val title = intent.getStringExtra("title") ?: "予定"
        val category = intent.getStringExtra("category") ?: ""
        val builder = NotificationCompat.Builder(context, "your_channel_id")
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("$title の予定")
            .setContentText("$category の予定がまもなく始まります")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        NotificationManagerCompat.from(context).notify(eventId.hashCode(), builder.build())
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "your_channel_id",
                "通知チャンネル名",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "通知の説明"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}