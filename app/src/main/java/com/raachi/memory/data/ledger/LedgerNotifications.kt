package com.raachi.memory.data.ledger

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
import androidx.core.net.toUri
import com.raachi.memory.MainActivity
import com.raachi.memory.R
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.usecase.ledgerShareMessage

private const val LEDGER_CHANNEL_ID = "ledger_alerts_raachi_sound_v3"
private const val PREVIOUS_LEDGER_CHANNEL_ID = "ledger_alerts_raachi_sound_v2"
private const val FIRST_CUSTOM_LEDGER_CHANNEL_ID = "ledger_alerts_raachi_sound"
private const val LEGACY_LEDGER_CHANNEL_ID = "ledger_alerts"

fun createLedgerNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        LEDGER_CHANNEL_ID,
        context.getString(R.string.ledger_channel_name),
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = context.getString(R.string.ledger_channel_description)
        setSound(raachiSoundUri(context), notificationAudioAttributes())
        enableVibration(true)
    }
    context.getSystemService(NotificationManager::class.java).apply {
        createNotificationChannel(channel)
        deleteNotificationChannel(PREVIOUS_LEDGER_CHANNEL_ID)
        deleteNotificationChannel(FIRST_CUSTOM_LEDGER_CHANNEL_ID)
        deleteNotificationChannel(LEGACY_LEDGER_CHANNEL_ID)
    }
}

fun showLedgerNotification(context: Context, entry: LedgerEntry) {
    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) return

    val openApp = PendingIntent.getActivity(
        context,
        ledgerRequestCode(entry.id),
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val returnedAction = Intent(context, LedgerAlertReceiver::class.java).apply {
        action = LedgerAlertReceiver.ACTION_MARK_RETURNED
        putExtra(LedgerAlertReceiver.EXTRA_LEDGER_ID, entry.id)
    }
    val markReturned = PendingIntent.getBroadcast(
        context,
        ledgerRequestCode(entry.id) xor 0x524554,
        returnedAction,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val builder = NotificationCompat.Builder(context, LEDGER_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(context.getString(R.string.ledger_due_notification, entry.personName))
        .setContentText(entry.notes ?: context.getString(R.string.ledger_due_notification_body))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setSound(raachiSoundUri(context))
        .setAutoCancel(true)
        .setContentIntent(openApp)
        .addAction(0, context.getString(R.string.mark_returned), markReturned)

    entry.mobileNumber?.let { mobile ->
        val url = "https://wa.me/91$mobile?text=${Uri.encode(ledgerShareMessage(entry))}".toUri()
        val whatsapp = PendingIntent.getActivity(
            context,
            ledgerRequestCode(entry.id) xor 0x5741,
            Intent(Intent.ACTION_VIEW, url),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        builder.addAction(0, context.getString(R.string.whatsapp), whatsapp)
    }
    NotificationManagerCompat.from(context).notify(ledgerNotificationId(entry.id), builder.build())
}

internal fun ledgerNotificationId(entryId: Long): Int = ledgerRequestCode(entryId) xor 0x4E4F5449

private fun raachiSoundUri(context: Context): Uri = Uri.Builder()
    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
    .authority(context.packageName)
    .appendPath("raw")
    .appendPath("raachisound")
    .build()

private fun notificationAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
    .build()
