package com.example.cloudbackupapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context.applicationContext) }
    val repo = remember { CloudDocRepo(context.applicationContext) }
    val config by store.cloudConfig.collectAsState(initial = CloudConfig(null, "Backup Folder"))

    val pickFolder = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        // Persist access
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        CoroutineScope(Dispatchers.Main).launch {
            store.setCloudTree(uri, "Backup Folder")
            repo.rootFromTree(uri)?.let { root -> repo.ensureAppFolders(root) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3F6))
            .padding(horizontal = 16.dp)
    ) {
        SimpleTopBar(title = "Settings", onBack = onBack)

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Linked cloud folder", fontWeight = FontWeight.SemiBold)
                Text(
                    text = config.treeUri?.toString() ?: "Not connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B6B6B)
                )

                Spacer(Modifier.height(6.dp))

                SettingsButton(
                    text = "Pick backup folder (Google Drive / Dropbox)",
                    onClick = { pickFolder.launch(null) }
                )
                SettingsButton(
                    text = "Open linked folder",
                    enabled = config.treeUri != null,
                    onClick = {
                        config.treeUri?.let { openTree(context, it) }
                    }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("How cloud works", fontWeight = FontWeight.SemiBold)
                Text(
                    "This app uses Android's folder picker. You can choose a folder inside Google Drive or Dropbox. " +
                            "The app reads your backups from that folder.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF555555)
                )
            }
        }
    }
}

@Composable
private fun SettingsButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val src = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(src)
            .clickable(
                enabled = enabled,
                interactionSource = src,
                indication = rememberRipple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3F6))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun openTree(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = uri
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(intent)
}
