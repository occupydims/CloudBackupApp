package com.example.cloudbackupapp.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Stub service required for Default SMS role eligibility.
 */
class RespondViaMessageService : Service() {
    override fun onBind(intent: Intent): IBinder? = null
}
