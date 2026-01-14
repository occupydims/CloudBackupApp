package com.example.cloudbackupapp

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
// Use AnimatedButton/AnimatedOutlinedButton to centralize tap animations
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

@Composable
fun CallLogBackupsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context.applicationContext) }
    val repo = remember { CloudDocRepo(context.applicationContext) }
    val config by store.cloudConfig.collectAsState(initial = CloudConfig(null, "Backup Folder"))

    var files by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }
    var selected by remember { mutableStateOf<DocumentFile?>(null) }

    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }

    LaunchedEffect(config.treeUri) {
        val root = repo.rootFromTree(config.treeUri)
        val dir = root?.findFile("CallLogs")
        files = dir?.let { repo.listFiles(it).sortedByDescending { f -> f.lastModified() } } ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3F6))
            .padding(horizontal = 16.dp)
    ) {
        SimpleTopBar(title = if (selected == null) "Call logs" else (selected?.name ?: "Call log backup"), onBack = {
            if (selected != null) selected = null else onBack()
        })

        if (config.treeUri == null) {
            InfoCard("No cloud folder connected", "Go to Settings and pick a Google Drive or Dropbox folder.")
            return@Column
        }

        if (selected == null) {
            if (files.isEmpty()) {
                InfoCard("No call log backups found", "Put call log backup files (often .zip/.csv/.json) into CallLogs/.")
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
                    Text("File", fontWeight = FontWeight.SemiBold)
                    Text(file.name ?: "", style = MaterialTheme.typography.bodyMedium)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AnimatedButton(onClick = { CloudIntents.openFile(context, file.uri, file.type) }) {
                            Text("Open")
                        }
                        AnimatedOutlinedButton(onClick = { /* placeholder for share */ CloudIntents.openFile(context, file.uri, file.type) }) {
                            Text("Share")
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Restore into the system Call Log is restricted on modern Android (requires the default dialer role and additional permissions). " +
                                "This build supports viewing/downloading the backup files; call log restore will be added after the dialer-role flow is implemented.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
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
