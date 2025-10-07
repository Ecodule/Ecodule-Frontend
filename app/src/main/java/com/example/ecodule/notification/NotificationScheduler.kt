package com.example.ecodule.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.ecodule.ui.CalendarContent.model.CalendarEvent
import java.util.Date
import java.time.LocalDateTime
import java.time.ZoneId

object NotificationScheduler {
    fun scheduleNotification(context: Context, event: CalendarEvent, userId: String) {
        val minutesBefore = event.notificationMinutes

        val eventStartMillis: Long? = when (val startDate = event.startDate) {
            is Date -> startDate.time
            is LocalDateTime -> startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            else -> null
        }
        if (eventStartMillis == null) return

        val notifyTimeMillis = eventStartMillis - (minutesBefore * 60 * 1000)
        Log.d("NotificationScheduler", "通知予定: $notifyTimeMillis, 現在: ${System.currentTimeMillis()}, 分前: $minutesBefore, イベント開始: $eventStartMillis")

        if (notifyTimeMillis < System.currentTimeMillis()) {
            Log.d("NotificationScheduler", "通知時刻が過去なのでセットしません")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return
            }
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("event_id", event.id)
            putExtra("title", event.label)
            putExtra("category", event.category)
            putExtra("user_id", userId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notifyTimeMillis,
            pendingIntent
        )
    }

    // 削除や更新用にキャンセルを用意
    fun cancelNotification(context: Context, eventId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    // 更新時に使えるユーティリティ（呼び出し側で利用）
    fun rescheduleNotification(context: Context, event: CalendarEvent, userId: String) {
        cancelNotification(context, event.id)
        scheduleNotification(context, event, userId)
    }
}