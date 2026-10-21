package com.kaezzy.app.core.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kaezzy.app.MainActivity
import com.kaezzy.app.core.database.KaezzyDatabase
import com.kaezzy.app.core.database.entity.ReminderEntity

object ReminderScheduler {
    const val CHANNEL_ID = "kaezzy_reminders"
    const val CHANNEL_NAME = "Pengingat Kaezzy"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat tugas, daily log, dan review skill Kaezzy"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(context: Context, reminder: ReminderEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
            putExtra("TITLE", reminder.title)
            putExtra("BODY", reminder.body)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.triggerAt,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                reminder.triggerAt,
                pendingIntent
            )
        }
    }

    suspend fun reconcileReminders(context: Context) {
        val database = KaezzyDatabase.getDatabase(context)
        val now = System.currentTimeMillis()
        val futureReminders = database.reminderDao().getActiveFutureRemindersDirect(now)
        for (reminder in futureReminders) {
            scheduleReminder(context, reminder)
        }
    }
}
