package com.qook.ftele.engine

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import com.qook.ftele.data.RuleStore
import com.qook.ftele.model.LogEntry
import com.qook.ftele.model.Rule

object SmsInboxHelper {

    private const val TAG = "SmsInboxHelper"

    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Reads past SMS messages from the device inbox (including before app installation)
     * and evaluates each against active rules so the user has immediate visibility.
     */
    fun fetchInboxMessages(
        context: Context,
        rules: List<Rule>,
        mode: String,
        limit: Int = 1000
    ): List<LogEntry> {
        if (!hasSmsPermission(context)) return emptyList()

        val list = mutableListOf<LogEntry>()
        val uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndex(Telephony.Sms._ID)
                val addressCol = c.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyCol = c.getColumnIndex(Telephony.Sms.BODY)
                val dateCol = c.getColumnIndex(Telephony.Sms.DATE)

                var count = 0
                while (c.moveToNext() && count < limit) {
                    val id = if (idCol >= 0) c.getString(idCol) else count.toString()
                    val sender = if (addressCol >= 0) c.getString(addressCol) ?: "Unknown" else "Unknown"
                    val text = if (bodyCol >= 0) c.getString(bodyCol) ?: "" else ""
                    val date = if (dateCol >= 0) c.getLong(dateCol) else System.currentTimeMillis()

                    if (RuleStore.isBackgroundNoise(sender, text)) continue
                    if (text.isBlank()) continue

                    // 1. If sender is in saved contacts, it is always allowed
                    val isContact = ContactHelper.isContact(context, sender)
                    val (isMuted, reason) = if (isContact) {
                        false to "Allowed: Saved Contact"
                    } else {
                        val decision = Patterns.decide(rules, mode, sender, text)
                        decision.mute to decision.reason
                    }

                    list.add(
                        LogEntry(
                            id = "sms_inbox_$id",
                            sender = sender.ifBlank { "Unknown" },
                            text = text,
                            time = date,
                            muted = isMuted,
                            reason = reason
                        )
                    )
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed reading SMS inbox messages", e)
        }

        return list
    }
}
