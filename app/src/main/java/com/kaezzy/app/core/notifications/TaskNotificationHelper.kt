package com.kaezzy.app.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kaezzy.app.MainActivity
import com.kaezzy.app.R
import com.kaezzy.app.core.database.entity.TaskEntity

object TaskNotificationHelper {

    fun showUncompletedTasksNotification(context: Context, todoTasks: List<TaskEntity>) {
        if (todoTasks.isEmpty()) return

        ReminderScheduler.createNotificationChannel(context)

        val title = "📋 Pengingat Tugas Belum Selesai"
        val bodyBuilder = StringBuilder()

        if (todoTasks.size == 1) {
            val task = todoTasks.first()
            bodyBuilder.append("${task.title} (${task.category})")
            if (task.description.isNotBlank()) {
                bodyBuilder.append("\n${task.description}")
            }
        } else {
            bodyBuilder.append("Lu punya ${todoTasks.size} tugas belum dikerjakan, brok:\n")
            todoTasks.take(4).forEach { t ->
                bodyBuilder.append("• ${t.title} (${t.category})\n")
            }
            if (todoTasks.size > 4) {
                bodyBuilder.append("...dan ${todoTasks.size - 4} tugas lainnya.")
            }
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            "TODO_TASKS_NOTIFICATION".hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bodyText = bodyBuilder.toString().trim()

        val builder = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.faviconk)
            .setContentTitle(title)
            .setContentText(if (todoTasks.size == 1) bodyText else "Ada ${todoTasks.size} tugas yang belum dikerjakan.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(9999, builder.build())
        } catch (_: SecurityException) {
            // Permission missing or denied
        }
    }

    fun scheduleTaskReminder(context: Context, task: TaskEntity) {
        if (task.status != "TODO") return

        ReminderScheduler.createNotificationChannel(context)

        val reminderBody = if (task.description.isNotBlank()) {
            "Kategori: ${task.category}\n${task.description}"
        } else {
            "Kategori: ${task.category} • Jangan lupa diselesaikan, brok!"
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_ID", task.id)
            putExtra("TITLE", "📌 Tugas: ${task.title}")
            putExtra("BODY", reminderBody)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = task.reminderAt ?: (System.currentTimeMillis() + 5_000L)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (_: SecurityException) {
        }
    }
}
