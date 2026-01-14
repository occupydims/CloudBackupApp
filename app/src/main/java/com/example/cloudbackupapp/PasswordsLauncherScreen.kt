package com.example.cloudbackupapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun PasswordsLauncherScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        openGooglePasswordManager(context)
        onBack()
    }
}

fun openGooglePasswordManager(context: Context) {
    val pkg = "com.google.android.apps.credentialmanager"
    val pm = context.packageManager
    val launch = pm.getLaunchIntentForPackage(pkg)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (launch != null) {
        context.startActivity(launch)
        return
    }

    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(market)
    } catch (_: ActivityNotFoundException) {
        val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(web)
    }
}
