package com.raachi.memory.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.raachi.memory.MainActivity
import com.raachi.memory.R
import com.raachi.memory.domain.model.Reminder

private const val REMINDER_CHANNEL_ID = "reminders_raachi_sound"
private const val LEGACY_REMINDER_CHANNEL_ID = "reminders"
private const val SILENT_REMINDER_CHANNEL_ID = "reminders_silent"

fun createReminderNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        REMINDER_CHANNEL_ID,
        context.getString(R.string.reminder_channel_name),
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = context.getString(R.string.reminder_channel_description)
        setSound(raachiSoundUri(context), notificationAudioAttributes())
        enableVibration(true)
    }
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    notificationManager.createNotificationChannel(channel)
    val silentChannel = NotificationChannel(
        SILENT_REMINDER_CHANNEL_ID,
        context.getString(R.string.silent_reminder_channel_name),
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = context.getString(R.string.silent_reminder_channel_description)
        setSound(null, null)
        enableVibration(false)
    }
    notificationManager.createNotificationChannel(silentChannel)
    notificationManager.deleteNotificationChannel(LEGACY_REMINDER_CHANNEL_ID)
}

fun showReminderNotification(context: Context, reminder: Reminder, soundEnabled: Boolean) {
    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) return

    val openApp = PendingIntent.getActivity(
        context,
        reminder.id.requestCode(),
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val channelId = if (soundEnabled) REMINDER_CHANNEL_ID else SILENT_REMINDER_CHANNEL_ID
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(reminder.title)
        .setContentText(reminder.description ?: context.getString(R.string.reminder_due_now))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setAutoCancel(true)
        .setContentIntent(openApp)
        .setOnlyAlertOnce(false)
        .setSound(if (soundEnabled) raachiSoundUri(context) else null)
        .setVibrate(if (reminder.vibrationEnabled) longArrayOf(0, 250, 150, 250) else null)
        .addAction(0, context.getString(R.string.snooze), actionPendingIntent(context, reminder.id, ReminderAlarmReceiver.ACTION_SNOOZE))
        .addAction(0, context.getString(R.string.done), actionPendingIntent(context, reminder.id, ReminderAlarmReceiver.ACTION_DONE))
        .addAction(0, context.getString(R.string.skip), actionPendingIntent(context, reminder.id, ReminderAlarmReceiver.ACTION_SKIP))
        .build()
    NotificationManagerCompat.from(context).notify(reminder.id.requestCode(), notification)
}

private fun raachiSoundUri(context: Context): Uri = Uri.parse(
    "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.raachisound}",
)

private fun notificationAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
    .build()

private fun actionPendingIntent(context: Context, id: Long, actionName: String): PendingIntent {
    val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
        action = actionName
        putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, id)
    }
    return PendingIntent.getBroadcast(
        context,
        id.requestCode() xor actionName.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
