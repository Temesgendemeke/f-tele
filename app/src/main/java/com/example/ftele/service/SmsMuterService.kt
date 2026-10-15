package com.example.ftele.service

import android.app.Notification
import android.content.Context
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.ftele.data.RuleStore

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
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString() ?: ""
        if (title.isEmpty() && text.isEmpty()) return

        val decision = RuleStore.decide(ctx, title, text)

        if (decision.mute) {
            cancelNotification(sbn.key)
        }

        // SMS apps re-post the same notification when it updates; log each message once.
        val fingerprint = "${sbn.key}|$title|$text"
        if (recent.add(fingerprint)) {
            if (recent.size > 50) {
                recent.remove(recent.first())
            }
            RuleStore.addLog(ctx, title, text, decision)
        }
    }

    private fun isSmsApp(ctx: Context, pkg: String): Boolean {
        if (pkg in KNOWN_SMS_PACKAGES) return true
        return try {
            Telephony.Sms.getDefaultSmsPackage(ctx) == pkg
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        private const val TAG = "SmsMuter"

        private val KNOWN_SMS_PACKAGES = setOf(
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.android.mms",
            "com.android.messaging"
        )
    }
}
