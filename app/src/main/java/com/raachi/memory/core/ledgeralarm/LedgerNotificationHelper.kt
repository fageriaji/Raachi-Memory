package com.raachi.memory.core.ledgeralarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.raachi.memory.MainActivity
import com.raachi.memory.R
import com.raachi.memory.domain.model.ItemType
import com.raachi.memory.domain.model.LedgerEntry
import com.raachi.memory.domain.util.DateTimeUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LedgerNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager
) {

    init {
        createNotificationChannel()
    }

    fun showLedgerNotification(entry: LedgerEntry) {
        if (!canPostNotifications()) {
            return
        }

        val dueDateTime = entry.dueDateTime ?: return
        val itemDescription = itemDescription(entry)

        val notificationText = context.getString(
            R.string.ledger_notification_content,
            itemDescription,
            DateTimeUtils.formatDate(dueDateTime),
            DateTimeUtils.formatTime(dueDateTime)
        )

        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(
                context.getString(
                    R.string.ledger_notification_title,
                    entry.personName
                )
            )
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(createOpenAppPendingIntent(entry.id))
            .addAction(
                0,
                context.getString(R.string.ledger_mark_returned),
                createActionPendingIntent(
                    entryId = entry.id,
                    action = ACTION_MARK_RETURNED
                )
            )
            .addAction(
                0,
                context.getString(R.string.ledger_notification_snooze),
                createActionPendingIntent(
                    entryId = entry.id,
                    action = ACTION_SNOOZE
                )
            )
            .addAction(
                0,
                context.getString(R.string.ledger_notification_share),
                createActionPendingIntent(
                    entryId = entry.id,
                    action = ACTION_SHARE
                )
            )
            .build()

        notificationManager.notify(notificationIdFor(entry.id), notification)
    }

    fun cancelNotification(entryId: Int) {
        notificationManager.cancel(notificationIdFor(entryId))
    }

    fun shareLedgerReminder(entry: LedgerEntry) {
        val dueDateTime = entry.dueDateTime ?: return

        val message = context.getString(
            R.string.ledger_share_message,
            entry.personName,
            itemDescription(entry),
            DateTimeUtils.formatDate(dueDateTime),
            DateTimeUtils.formatTime(dueDateTime)
        )

        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage(WHATSAPP_PACKAGE)
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(whatsappIntent)
        } catch (_: ActivityNotFoundException) {
            openAndroidSharesheet(message)
        } catch (_: Exception) {
            openAndroidSharesheet(message)
        }
    }

    private fun openAndroidSharesheet(message: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

        val chooserIntent = Intent.createChooser(
            shareIntent,
            context.getString(R.string.ledger_share_chooser_title)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooserIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.ledger_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(
                R.string.ledger_notification_channel_description
            )
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun createOpenAppPendingIntent(entryId: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_LEDGER_ID, entryId)
        }

        return PendingIntent.getActivity(
            context,
            notificationIdFor(entryId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createActionPendingIntent(
        entryId: Int,
        action: String
    ): PendingIntent {
        val intent = Intent(
            context,
            LedgerNotificationActionReceiver::class.java
        ).apply {
            this.action = action
            putExtra(EXTRA_LEDGER_ID, entryId)
        }

        return PendingIntent.getBroadcast(
            context,
            actionRequestCodeFor(entryId, action),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun itemDescription(entry: LedgerEntry): String {
        entry.amount?.let { amount ->
            return context.getString(R.string.ledger_amount_format, amount)
        }

        val itemName = entry.itemName
            ?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.not_set)

        return context.getString(
            R.string.ledger_item_format,
            context.getString(entry.itemType.labelResId()),
            itemName
        )
    }

    private fun ItemType.labelResId(): Int {
        return when (this) {
            ItemType.MONEY -> R.string.ledger_item_type_money
            ItemType.BOOK -> R.string.ledger_item_type_book
            ItemType.CHARGER -> R.string.ledger_item_type_charger
            ItemType.DOCUMENTS -> R.string.ledger_item_type_documents
            ItemType.OTHER -> R.string.ledger_item_type_other
        }
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun notificationIdFor(entryId: Int): Int {
        return NOTIFICATION_ID_OFFSET + entryId
    }

    private fun actionRequestCodeFor(
        entryId: Int,
        action: String
    ): Int {
        val actionOffset = when (action) {
            ACTION_MARK_RETURNED -> 1
            ACTION_SNOOZE -> 2
            ACTION_SHARE -> 3
            else -> 0
        }

        return ACTION_REQUEST_CODE_OFFSET + (entryId * 10) + actionOffset
    }

    companion object {
        const val CHANNEL_ID = "LEDGER_REMINDER_CHANNEL"

        const val EXTRA_LEDGER_ID = "LEDGER_ID"

        const val ACTION_MARK_RETURNED =
            "com.raachi.memory.action.LEDGER_MARK_RETURNED"

        const val ACTION_SNOOZE =
            "com.raachi.memory.action.LEDGER_SNOOZE"

        const val ACTION_SHARE =
            "com.raachi.memory.action.LEDGER_SHARE"

        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val NOTIFICATION_ID_OFFSET = 300_000
        private const val ACTION_REQUEST_CODE_OFFSET = 400_000
    }
}