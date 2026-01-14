package com.example.cloudbackupapp.sms

import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Minimal SENDTO handler required for Default SMS role eligibility.
 * This app isn't a full messaging client yet; the UI is intentionally minimal.
 */
class SmsComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No UI for now.
        finish()
    }
}
