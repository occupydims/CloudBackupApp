package com.example.cloudbackupapp

import android.content.Context
import android.content.Intent
import android.net.Uri

object CloudIntents {
    fun openFile(context: Context, uri: Uri, mime: String?) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { context.startActivity(intent) }
    }
}
