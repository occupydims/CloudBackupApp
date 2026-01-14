package com.example.cloudbackupapp

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun StorageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context.applicationContext) }
    val repo = remember { CloudDocRepo(context.applicationContext) }
    val config by store.cloudConfig.collectAsState(initial = CloudConfig(null, "Backup Folder"))

    var currentDir by remember { mutableStateOf<DocumentFile?>(null) }
    var pathStack by remember { mutableStateOf(listOf<DocumentFile>()) }
    var dirs by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }
    var files by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }

    LaunchedEffect(config.treeUri, currentDir) {
        val root = repo.rootFromTree(config.treeUri)
        val dir = currentDir ?: root
        if (dir == null) {
            dirs = emptyList(); files = emptyList(); return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            dirs = repo.listDirs(dir)
            files = repo.listFiles(dir).sortedByDescending { it.lastModified() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3F6))
            .padding(horizontal = 16.dp)
    ) {
        SimpleTopBar(
            title = "Storage",
            onBack = {
                // If browsing inside a folder, go up; else exit.
                if (pathStack.isNotEmpty()) {
                    val newStack = pathStack.dropLast(1)
                    pathStack = newStack
                    currentDir = newStack.lastOrNull()
                } else {
                    onBack()
                }
            }
        )

        if (config.treeUri == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("No cloud folder connected", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Go to Settings and pick a Google Drive or Dropbox folder.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF555555)
                    )
                }
            }
            return@Column
        }

        Spacer(Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (dirs.isNotEmpty()) {
                item {
                    Text("Folders", fontWeight = FontWeight.SemiBold, color = Color(0xFF6B6B6B))
                }
                items(dirs) { d ->
                    FileRow(
                        title = d.name ?: "(folder)",
                        subtitle = "Folder",
                        onClick = {
                            pathStack = pathStack + d
                            currentDir = d
                        }
                    )
                }
            }

            if (files.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Files", fontWeight = FontWeight.SemiBold, color = Color(0xFF6B6B6B))
                }
                items(files) { f ->
                    FileRow(
                        title = f.name ?: "(file)",
                        subtitle = "${f.type ?: "file"}",
                        onClick = {
                            // Best-effort open
                            CloudIntents.openFile(context, f.uri, f.type)
                        }
                    )
                }
            }
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
