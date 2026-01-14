package com.example.cloudbackupapp

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
// Use AnimatedButton/AnimatedOutlinedButton to centralize tap animations
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
 
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsBackupsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context.applicationContext) }
    val repo = remember { CloudDocRepo(context.applicationContext) }
    val config by store.cloudConfig.collectAsState(initial = CloudConfig(null, "Backup Folder"))

    var files by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }
    var selected by remember { mutableStateOf<DocumentFile?>(null) }
    var showRangePicker by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }

    // Role/default-SMS request launcher
    val requestDefaultSms = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        status = if (result.resultCode == Activity.RESULT_OK) "Default SMS granted. Try restore again." else "Default SMS not granted."
    }

    LaunchedEffect(config.treeUri) {
        val root = repo.rootFromTree(config.treeUri)
        val smsDir = root?.findFile("SMS")
        files = smsDir?.let { repo.listFiles(it).sortedByDescending { f -> f.lastModified() } } ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3F6))
            .padding(horizontal = 16.dp)
    ) {
        SimpleTopBar(title = if (selected == null) "SMS" else (selected?.name ?: "SMS backup"), onBack = {
            if (selected != null) selected = null else onBack()
        })

        if (config.treeUri == null) {
            InfoCard("No cloud folder connected", "Go to Settings and pick a Google Drive or Dropbox folder.")
            return@Column
        }

        status?.let {
            Spacer(Modifier.height(8.dp))
            InfoCard("Status", it)
            Spacer(Modifier.height(8.dp))
        }

        if (selected == null) {
            if (files.isEmpty()) {
                InfoCard("No SMS backups found", "Put SMS backup files into SMS/ in your linked folder (XML format supported).")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(files) { f ->
                        FileRow(
                            title = f.name ?: "(file)",
                            subtitle = "${dateFmt.format(Date(f.lastModified()))}",
                            onClick = { selected = f }
                        )
                    }
                }
            }
        } else {
            val file = selected!!
            Spacer(Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Restore options", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Restore writes messages back into your phone's system Messages database. Android requires this app to be set as the Default SMS app during restore.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF555555)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AnimatedButton(
                            onClick = {
                                // Attempt restore all
                                val ok = SmsRestoreManager.tryRestore(context, file.uri, null, null)
                                if (!ok) {
                                    launchDefaultSmsRequest(context, requestDefaultSms)
                                } else {
                                    status = "Restore completed (all)."
                                }
                            }
                        ) { Text("Restore all") }
                        AnimatedOutlinedButton(onClick = { showRangePicker = true }) { Text("Restore date range") }
                    }
                }
            }
        }

        if (showRangePicker && selected != null) {
            DateRangePickerDialog(
                onDismiss = { showRangePicker = false },
                onConfirm = { startMs, endMs ->
                    val file = selected!!
                    val ok = SmsRestoreManager.tryRestore(context, file.uri, startMs, endMs)
                    if (!ok) {
                        launchDefaultSmsRequest(context, requestDefaultSms)
                    } else {
                        status = "Restore completed (range)."
                    }
                    showRangePicker = false
                }
            )
        }
    }
}

private fun launchDefaultSmsRequest(
    context: android.content.Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    val intent = SmsRestoreManager.createDefaultSmsRequestIntent(context)
    launcher.launch(intent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (startMs: Long?, endMs: Long?) -> Unit
) {
    val state = rememberDateRangePickerState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select date range") },
        text = { DateRangePicker(state = state) },
        confirmButton = {
            AnimatedButton(onClick = { onConfirm(state.selectedStartDateMillis, state.selectedEndDateMillis) }) {
                Text("Restore")
            }
        },
        dismissButton = {
            AnimatedOutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF555555))
        }
    }
}

@Composable
private fun FileRow(title: String, subtitle: String, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(src)
            .clickable(interactionSource = src, indication = rememberRipple(), onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF777777))
        }
    }
}
