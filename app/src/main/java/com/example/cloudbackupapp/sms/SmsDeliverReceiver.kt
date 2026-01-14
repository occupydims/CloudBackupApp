package com.example.cloudbackupapp.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Stub receiver required for Default SMS role eligibility.
 * We don't process incoming SMS yet.
 */
class SmsDeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // No-op
    }
}
