package com.raachi.memory.core.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.raachi.memory.MainActivity
import com.raachi.memory.R
import com.raachi.memory.domain.model.Reminder
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    private val context: Context,
    private val notificationManager: NotificationManager
) {
    companion object {
        const val CHANNEL_ID = "REMINDER_CHANNEL"
        const val ACTION_DONE = "ACTION_DONE"
        const val ACTION_SNOOZE = "ACTION_SNOOZE"
        const val ACTION_SKIP = "ACTION_SKIP"
        const val EXTRA_REMINDER_ID = "REMINDER_ID"
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Health Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for water, medicine, and routines."
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(reminder: Reminder) {
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            reminder.id,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your actual icon
            .setContentTitle(reminder.title)
            .setContentText(
                reminder.description ?: "Time for your ${reminder.category.name.lowercase()} routine."
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                0,
                context.getString(R.string.snooze),
                createActionIntent(reminder.id, ACTION_SNOOZE)
            )
            .addAction(
                0,
                context.getString(R.string.done),
                createActionIntent(reminder.id, ACTION_DONE)
            )
            .addAction(
                0,
                context.getString(R.string.skip),
                createActionIntent(reminder.id, ACTION_SKIP)
            )
            .build()

        notificationManager.notify(reminder.id, notification)
    }

    private fun createActionIntent(reminderId: Int, action: String): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }

        // Ensure deterministic uniqueness to prevent PendingIntent collision overrides
        val actionCode = when(action) {
            ACTION_DONE -> 1
            ACTION_SNOOZE -> 2
            ACTION_SKIP -> 3
            else -> 0
        }

        return PendingIntent.getBroadcast(
            context,
            reminderId * 10 + actionCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancelNotification(reminderId: Int) {
        notificationManager.cancel(reminderId)
    }
}