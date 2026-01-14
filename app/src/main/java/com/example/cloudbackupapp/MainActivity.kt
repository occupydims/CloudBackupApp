package com.example.cloudbackupapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Google Mobile Ads SDK (uses the App ID in AndroidManifest.xml)
        MobileAds.initialize(this)

        setContent {
            MaterialTheme {
                Surface {
                    AppNav()
                }
            }
        }
    }
}
