package com.qook.ftele.service

import android.app.Notification
import android.content.Context
import android.os.Bundle
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.qook.ftele.data.RuleStore
import com.qook.ftele.engine.Patterns
import com.qook.ftele.model.LogEntry

/**
 * Watches notifications from SMS apps. When a message matches a block rule (or is not on the
 * allow list in allowlist mode) the notification is dismissed. The SMS itself stays in the inbox.
 */
class SmsMuterService : NotificationListenerService() {

    private val recent = LinkedHashSet<String>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            handle(sbn)
        } catch (e: Exception) {
            Log.w(TAG, "Could not process notification", e)
        }
    }

    private fun handle(sbn: StatusBarNotification) {
        val ctx = applicationContext
        if (!RuleStore.isEnabled(ctx)) return
        if (sbn.packageName == packageName) return

        val n = sbn.notification
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        if (!isSmsApp(ctx, sbn.packageName)) return

        val extras = n.extras
        var title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        var text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString() ?: ""

        // Handle MessagingStyle notifications (Google Messages, etc.)
        if (text.isEmpty() || text.contains("Sensitive", ignoreCase = true)) {
            val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            if (messages != null && messages.isNotEmpty()) {
                val last = messages.last()
                if (last is Bundle) {
                    val msgText = last.getCharSequence("text")?.toString()
                    if (!msgText.isNullOrBlank()) {
                        text = msgText
                    }
                }
            }
        }

        // Fallback to tickerText if text is empty or masked
        if ((text.isEmpty() || text.contains("Sensitive", ignoreCase = true)) && n.tickerText != null) {
            val ticker = n.tickerText.toString()
            if (ticker.isNotBlank()) {
                text = ticker
            }
        }

        if (title.isBlank() && text.isBlank()) return

        // Deduplication to avoid processing the same notification repeatedly
        val dedupKey = "${sbn.key}|${sbn.postTime}|$title|$text"
        if (recent.contains(dedupKey)) return
        recent.add(dedupKey)
        while (recent.size > 200) {
            val it = recent.iterator()
            it.next()
            it.remove()
        }

        val rules = RuleStore.getAllEffectiveRules(ctx)
        val mode = RuleStore.getMode(ctx)
        val decision = Patterns.decide(rules, mode, title, text)

        if (decision.mute) {
            cancelNotification(sbn.key)
            Log.i(TAG, "Muted SMS notification from '$title': ${decision.reason}")
        }

        RuleStore.appendLog(
            ctx,
            LogEntry(
                id = "${sbn.postTime}_${sbn.id}",
                sender = title.ifBlank { "Unknown" },
                text = text,
                time = sbn.postTime,
                muted = decision.mute,
                reason = decision.reason
            )
        )
    }

    private fun isSmsApp(ctx: Context, pkg: String): Boolean {
        if (pkg.contains("mms", ignoreCase = true) || pkg.contains("sms", ignoreCase = true)) {
            return true
        }
        val defaultSms = Telephony.Sms.getDefaultSmsPackage(ctx)
        if (defaultSms != null && defaultSms == pkg) return true
        return KNOWN_SMS_PACKAGES.contains(pkg)
    }

    companion object {
        private const val TAG = "FTeleService"
        private val KNOWN_SMS_PACKAGES = setOf(
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.android.mms",
            "com.sonyericsson.conversations",
            "com.motorola.messaging",
            "com.asus.message",
            "com.oneplus.mms"
        )
    }
}
