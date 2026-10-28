package com.qook.ftele.engine

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.service.notification.StatusBarNotification
import androidx.core.content.ContextCompat

object ContactHelper {

    /**
     * Checks if the notification is from a saved contact.
     * Evaluates both notification metadata (EXTRA_PEOPLE, EXTRA_PEOPLE_LIST, MessagingStyle Person URIs)
     * and local ContactsContract if READ_CONTACTS permission is granted.
     */
    fun isContactNotification(context: Context, sbn: StatusBarNotification, senderTitle: String): Boolean {
        val n = sbn.notification
        val extras = n.extras

        // 1. Inspect Notification.EXTRA_PEOPLE (Standard Android contact lookup URI)
        @Suppress("DEPRECATION")
        val people = extras.getStringArray(Notification.EXTRA_PEOPLE)
        if (people != null && people.any { isContactUri(it) }) {
            return true
        }

        // 2. Inspect Notification.EXTRA_PEOPLE_LIST (API 28+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val peopleList = extras.getParcelableArrayList<android.app.Person>(Notification.EXTRA_PEOPLE_LIST)
            if (peopleList != null && peopleList.any { it.uri?.let { uri -> isContactUri(uri) } == true }) {
                return true
            }
        }

        // 3. Inspect AndroidX NotificationCompat.MessagingStyle person URIs
        try {
            val style = androidx.core.app.NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(n)
            if (style != null) {
                if (style.messages.any { msg ->
                    msg.person?.uri?.let { isContactUri(it) } == true
                }) {
                    return true
                }
            }
        } catch (_: Exception) {}

        // 4. Query local Contacts Provider if permission is granted
        if (hasContactPermission(context) && senderTitle.isNotBlank()) {
            if (isSavedInPhonebook(context, senderTitle)) {
                return true
            }
        }

        return false
    }

    fun hasContactPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if a phone number or sender display name exists in the user's saved contacts.
     */
    fun isContact(context: Context, sender: String): Boolean {
        if (!hasContactPermission(context) || sender.isBlank()) return false
        return isSavedInPhonebook(context, sender)
    }

    private fun isContactUri(uriStr: String): Boolean {
        return uriStr.startsWith("content://com.android.contacts") ||
               uriStr.contains("contacts/lookup") ||
               uriStr.contains("contacts/people")
    }

    fun isSavedInPhonebook(context: Context, sender: String): Boolean {
        val clean = sender.trim()
        if (clean.isBlank()) return false

        // Check PhoneLookup for numbers (e.g. +251..., 09..., etc.)
        try {
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(clean))
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup._ID),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.count > 0) return true
            }
        } catch (_: Exception) {}

        // Check Contacts filter by display name (e.g. "Abebe Kebede", "Mom", etc.)
        try {
            val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(clean))
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.Contacts._ID),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.count > 0) return true
            }
        } catch (_: Exception) {}

        return false
    }
}
