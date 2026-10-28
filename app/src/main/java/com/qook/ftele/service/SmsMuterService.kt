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
        // Ignore ongoing or foreground service notifications (e.g. "Messages is doing work in the background")
        if (sbn.isOngoing) return
        if ((n.flags and (Notification.FLAG_ONGOING_EVENT or Notification.FLAG_FOREGROUND_SERVICE)) != 0) return
        if (n.category == Notification.CATEGORY_SERVICE || n.category == Notification.CATEGORY_PROGRESS) return

        // Skip group summaries during initial content extraction, but track if needed
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        if (!isMessagingNotification(ctx, sbn)) return

        val extras = n.extras
        var title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        var text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString() ?: ""

        // Extract using NotificationCompat.MessagingStyle (Google Messages, Samsung, Transsion, etc.)
        try {
            val messagingStyle = androidx.core.app.NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(n)
            if (messagingStyle != null) {
                val conversationTitle = messagingStyle.conversationTitle?.toString()
                if (!conversationTitle.isNullOrBlank()) {
                    title = conversationTitle
                }
                val messages = messagingStyle.messages
                if (messages.isNotEmpty()) {
                    val lastMsg = messages.last()
                    val lastText = lastMsg.text?.toString()
                    val senderName = lastMsg.person?.name?.toString()
                    if (title.isBlank() && !senderName.isNullOrBlank()) {
                        title = senderName
                    }
                    if (!lastText.isNullOrBlank()) {
                        // If current text is empty, generic like "2 new messages", or sensitive, replace with real message text
                        if (text.isBlank() ||
                            text.contains("Sensitive", ignoreCase = true) ||
                            text.matches(Regex("^\\d+\\s+.*message.*", RegexOption.IGNORE_CASE))
                        ) {
                            text = lastText
                        } else {
                            // Also append or combine so rules matching either will catch it
                            text = "$text\n$lastText"
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // Fallback for Bundle-based EXTRA_MESSAGES
        if (text.isEmpty() ||
            text.contains("Sensitive", ignoreCase = true) ||
            text.matches(Regex("^\\d+\\s+.*message.*", RegexOption.IGNORE_CASE))
        ) {
            @Suppress("DEPRECATION")
            val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            if (messages != null && messages.isNotEmpty()) {
                val last = messages.last()
                if (last is Bundle) {
                    val msgText = last.getCharSequence("text")?.toString()
                    if (!msgText.isNullOrBlank()) {
                        text = msgText
                    }
                    val sender = last.getCharSequence("sender")?.toString()
                    if (title.isBlank() && !sender.isNullOrBlank()) {
                        title = sender
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

        // If title is still empty, check subtext
        if (title.isBlank()) {
            title = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
        }

        if (title.isBlank() && text.isBlank()) return
        if (RuleStore.isBackgroundNoise(title, text)) return

        // Deduplication to avoid processing the same notification repeatedly
        val dedupKey = "${sbn.key}|${sbn.postTime}|$title|$text"
        if (recent.contains(dedupKey)) return
        recent.add(dedupKey)
        while (recent.size > 200) {
            val it = recent.iterator()
            it.next()
            it.remove()
        }

        // 1. Priority Safety: All senders from phonebook contacts are ALWAYS allowed
        if (com.qook.ftele.engine.ContactHelper.isContactNotification(ctx, sbn, title)) {
            Log.i(TAG, "Allowed SMS notification from saved contact '$title'")
            RuleStore.appendLog(
                ctx,
                LogEntry(
                    id = "${sbn.postTime}_${sbn.id}",
                    sender = title.ifBlank { "Contact" },
                    text = text,
                    time = sbn.postTime,
                    muted = false,
                    reason = "Allowed: Saved Contact"
                )
            )
            return
        }

        val rules = RuleStore.getAllEffectiveRules(ctx)
        val mode = RuleStore.getMode(ctx)
        val decision = Patterns.decide(rules, mode, title, text)

        if (decision.mute) {
            cancelNotification(sbn.key)
            try {
                @Suppress("DEPRECATION")
                cancelNotification(sbn.packageName, sbn.tag, sbn.id)
            } catch (_: Exception) {}

            // If notification is part of a group, clean up orphaned group summary
            val groupKey = sbn.groupKey
            if (!groupKey.isNullOrBlank()) {
                try {
                    val active = activeNotifications ?: emptyArray()
                    val groupNotifs = active.filter { it.packageName == sbn.packageName && it.groupKey == groupKey }
                    val remainingChildren = groupNotifs.filter {
                        (it.notification.flags and Notification.FLAG_GROUP_SUMMARY) == 0 && it.key != sbn.key
                    }
                    if (remainingChildren.isEmpty()) {
                        groupNotifs.filter {
                            (it.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
                        }.forEach { summarySbn ->
                            cancelNotification(summarySbn.key)
                        }
                    }
                } catch (_: Exception) {}
            }

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

    private fun isMessagingNotification(ctx: Context, sbn: StatusBarNotification): Boolean {
        // Universal notification category for SMS & messaging
        if (sbn.notification.category == Notification.CATEGORY_MESSAGE) {
            return true
        }

        val pkg = sbn.packageName.lowercase()

        // Check if it's the device default SMS app
        try {
            val defaultSms = Telephony.Sms.getDefaultSmsPackage(ctx)
            if (defaultSms != null && defaultSms.equals(pkg, ignoreCase = true)) {
                return true
            }
        } catch (_: Exception) {}

        // Common SMS/MMS/Messaging keywords in package name
        if (pkg.contains("sms") || pkg.contains("mms") || pkg.contains("message") || pkg.contains("messaging")) {
            return true
        }

        return KNOWN_MESSAGING_PACKAGES.contains(pkg)
    }

    companion object {
        private const val TAG = "FTeleService"
        private val KNOWN_MESSAGING_PACKAGES = setOf(
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.android.mms",
            "com.sonyericsson.conversations",
            "com.motorola.messaging",
            "com.asus.message",
            "com.oneplus.mms",
            // Transsion (Tecno, Infinix, itel)
            "com.transsion.message",
            "com.transsion.messaging",
            "com.transsion.mms",
            "com.transsion.messagebox",
            "com.infinix.message",
            "com.itel.message",
            // Xiaomi / MIUI / HyperOS
            "com.miui.sms",
            "com.miui.mms",
            // Huawei / Honor
            "com.huawei.message",
            "com.hihonor.message",
            // Oppo / Realme / OnePlus ColorOS
            "com.coloros.mms",
            "com.oplus.mms",
            "com.oppo.mms",
            // Vivo / iQOO
            "com.vivo.mms",
            "com.bbk.mms",
            "com.vivo.message",
            // Truecaller (widely used default SMS handler in Africa)
            "com.truecaller",
            // Telebirr & Ethio Telecom promo pushes
            "cn.tydic.ethiopay",
            "com.ethiotelecom.myethiotel",
            // SIM Toolkit
            "com.android.stk",
            "com.android.stk2",
            // Popular 3rd party SMS apps
            "com.microsoft.android.smsorganizer",
            "com.simplemobiletools.smsmessenger",
            "com.fossify.messages",
            "com.moez.QKSMS",
            "com.mplus.sms",
            "com.p1.chompsms",
            "com.jb.gosms"
        )
    }
}
