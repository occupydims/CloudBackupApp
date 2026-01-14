package com.example.cloudbackupapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.app.role.RoleManager
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

data class SmsMessageRecord(
    val address: String?,
    val body: String?,
    val date: Long,
    val dateSent: Long?,
    val type: Int?,
    val read: Int?,
    val seen: Int?
)

object SmsRestoreManager {
    /**
     * Best-effort restore. Returns false if the platform denied writes (usually because we're not default SMS).
     */
    fun tryRestore(context: Context, backupUri: Uri, startMs: Long?, endMs: Long?): Boolean {
        return try {
            runBlocking {
                restoreInternal(context, backupUri, startMs, endMs)
            }
            true
        } catch (_: SecurityException) {
            false
        } catch (_: Throwable) {
            // Any parsing/opening issue should not crash the app; treat as not-restored and show via status UI.
            false
        }
    }

    fun createDefaultSmsRequestIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= 29) {
            val rm = context.getSystemService(RoleManager::class.java)
            rm.createRequestRoleIntent(RoleManager.ROLE_SMS)
        } else {
            Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            }
        }
    }

    private suspend fun restoreInternal(context: Context, backupUri: Uri, startMs: Long?, endMs: Long?) {
        val records = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(backupUri).use { input ->
                requireNotNull(input) { "Unable to open backup file" }
                parseSmsBackupRestoreXml(input.readBytes())
            }
        }

        val filtered = records.filter { r ->
            val t = r.date
            val afterStart = startMs?.let { t >= it } ?: true
            val beforeEnd = endMs?.let { t <= it } ?: true
            afterStart && beforeEnd
        }

        withContext(Dispatchers.IO) {
            val cr = context.contentResolver
            val uri = Telephony.Sms.CONTENT_URI

            // Insert one-by-one for simplicity (can be bulkInsert later).
            for (r in filtered) {
                val cv = ContentValues().apply {
                    put(Telephony.TextBasedSmsColumns.ADDRESS, r.address)
                    put(Telephony.TextBasedSmsColumns.BODY, r.body)
                    put(Telephony.TextBasedSmsColumns.DATE, r.date)
                    r.dateSent?.let { put(Telephony.TextBasedSmsColumns.DATE_SENT, it) }
                    r.type?.let { put(Telephony.TextBasedSmsColumns.TYPE, it) }
                    r.read?.let { put(Telephony.TextBasedSmsColumns.READ, it) }
                    r.seen?.let { put(Telephony.TextBasedSmsColumns.SEEN, it) }
                }
                cr.insert(uri, cv)
            }
        }
    }

    private fun parseSmsBackupRestoreXml(bytes: ByteArray): List<SmsMessageRecord> {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(bytes.inputStream(), "UTF-8")

        val out = ArrayList<SmsMessageRecord>(1024)
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name == "sms") {
                val address = parser.getAttributeValue(null, "address")
                val body = parser.getAttributeValue(null, "body")
                val date = parser.getAttributeValue(null, "date")?.toLongOrNull() ?: 0L
                val dateSent = parser.getAttributeValue(null, "date_sent")?.toLongOrNull()
                val type = parser.getAttributeValue(null, "type")?.toIntOrNull()
                val read = parser.getAttributeValue(null, "read")?.toIntOrNull()
                val seen = parser.getAttributeValue(null, "seen")?.toIntOrNull()

                out.add(
                    SmsMessageRecord(
                        address = address,
                        body = body,
                        date = date,
                        dateSent = dateSent,
                        type = type,
                        read = read,
                        seen = seen
                    )
                )
            }
            event = parser.next()
        }
        return out
    }
}
