package com.example.cloudbackupapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile

// Local helpers (kept simple; Photos uses similar UI)

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
private fun AlbumList(albums: List<DocumentFile>, onOpen: (DocumentFile) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        albums.forEach { album ->
            val src = remember { MutableInteractionSource() }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .pressScale(src)
                    .clickable(interactionSource = src, indication = rememberRipple()) { onOpen(album) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(album.name ?: "(folder)", fontWeight = FontWeight.SemiBold)
                    Text("Tap to view", style = MaterialTheme.typography.bodySmall, color = Color(0xFF777777))
                }
            }
        }
    }
}

@Composable
fun VideosScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context.applicationContext) }
    val repo = remember { CloudDocRepo(context.applicationContext) }
    val config by store.cloudConfig.collectAsState(initial = CloudConfig(null, "Backup Folder"))

    var inAlbum by remember { mutableStateOf<DocumentFile?>(null) }
    var albums by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }
    var videos by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }

    LaunchedEffect(config.treeUri, inAlbum) {
        val root = repo.rootFromTree(config.treeUri)
        val videosRoot = root?.findFile("Videos")

        if (videosRoot == null) {
            albums = emptyList(); videos = emptyList(); return@LaunchedEffect
        }

        if (inAlbum == null) {
            albums = repo.listDirs(videosRoot)
            videos = emptyList()
        } else {
            videos = repo.listFiles(inAlbum!!).sortedByDescending { it.lastModified() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3F6))
            .padding(horizontal = 16.dp)
    ) {
        SimpleTopBar(
            title = if (inAlbum == null) "Videos" else (inAlbum?.name ?: "Album"),
            onBack = {
                if (inAlbum != null) inAlbum = null else onBack()
            }
        )

        if (config.treeUri == null) {
            InfoCard("No cloud folder connected", "Go to Settings and pick a Google Drive or Dropbox folder.")
            return@Column
        }

        if (inAlbum == null) {
            if (albums.isEmpty()) {
                InfoCard("No folders found", "Create folders inside Videos/ in your linked cloud folder.")
            } else {
                Spacer(Modifier.height(8.dp))
                AlbumList(albums) { selected -> inAlbum = selected }
            }
        } else {
            if (videos.isEmpty()) {
                InfoCard("No videos found", "Put videos into this folder.")
            } else {
                Spacer(Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(videos) { file ->
                        val src = remember { MutableInteractionSource() }
                        Card(
                            modifier = Modifier
                                .height(150.dp)
                                .pressScale(src)
                                .clickable(interactionSource = src, indication = rememberRipple()) {
                                    CloudIntents.openFile(context, file.uri, file.type)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = file.name ?: "video",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
