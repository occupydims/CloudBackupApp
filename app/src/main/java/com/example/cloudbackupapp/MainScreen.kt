package com.example.cloudbackupapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

data class Tile(
    val title: String,
    val subtitle: String,
    val icon: @Composable () -> Unit
)

@Composable
fun MainScreen(
    onOpen: (String) -> Unit
) {
    val bg = Color(0xFFF3F3F6)

    val context = LocalContext.current

    val tiles = listOf(
        Tile("Photos", "46 items") { Icon(Icons.Filled.Image, contentDescription = null) },
        Tile("Call logs", "On") { Icon(Icons.Filled.Phone, contentDescription = null) },
        Tile("contacts", "1 item") { Icon(Icons.Filled.People, contentDescription = null) },
        Tile("passwords", "35 items") { Icon(Icons.Filled.Key, contentDescription = null) },
        Tile("Messages", "4.8 MB") { Icon(Icons.Filled.Message, contentDescription = null) },
        Tile("Videos", "4.2 KB") { Icon(Icons.Filled.PlayCircle, contentDescription = null) },
    )

    fun handleOpen(title: String) {
        if (title.equals("passwords", ignoreCase = true)) {
            openGooglePasswordManager(context)
        } else {
            onOpen(title)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(Modifier.height(12.dp)) }

        // Back button + header area (visual only)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { /* optional */ }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back")
                }
            }
        }

        item {
            Text(
                text = "Danny",
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF7A7A7A),
                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
            )
        }

        // Storage card (clickable row)
        item {
            val src = remember { MutableInteractionSource() }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .pressScale(src)
                    .clickable(
                        interactionSource = src,
                        indication = rememberRipple(),
                        onClick = { onOpen("Storage") }
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Storage", fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(10.dp))
                    LinearProgressIndicator(
                        progress = 0.06f,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "1.2 of 50 GB",
                        color = Color(0xFF808080),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF9A9A9A))
                }
            }
        }

        // Saved to Cloud section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val seeAllSrc = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF25C06D))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Saved to Cloud", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "See All",
                            color = Color(0xFF7D7D7D),
                            fontSize = 13.sp,
                            modifier = Modifier
                                .pressScale(seeAllSrc)
                                .clickable(
                                    interactionSource = seeAllSrc,
                                    indication = rememberRipple(),
                                    onClick = { onOpen("See All") }
                                )
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF9A9A9A),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // 2x3 grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SavedTile(tiles[0], Modifier.weight(1f), onClick = { handleOpen(tiles[0].title) })
                            SavedTile(tiles[1], Modifier.weight(1f), onClick = { handleOpen(tiles[1].title) })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SavedTile(tiles[2], Modifier.weight(1f), onClick = { handleOpen(tiles[2].title) })
                            SavedTile(tiles[3], Modifier.weight(1f), onClick = { handleOpen(tiles[3].title) })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SavedTile(tiles[4], Modifier.weight(1f), onClick = { handleOpen(tiles[4].title) })
                            SavedTile(tiles[5], Modifier.weight(1f), onClick = { handleOpen(tiles[5].title) })
                        }
                    }
                }
            }
        }

        // Bottom buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BigButton(
                    text = "+connect to Google drive",
                    onClick = { onOpen("Connect") }
                )
                BigButton(
                    text = "set backup schedule",
                    onClick = { onOpen("Schedule") }
                )

                // Square-style ad slot (Medium Rectangle 300x250)
                SquareAdSlot(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }

        item { Spacer(Modifier.height(22.dp)) }
    }
}

@Composable
private fun SquareAdSlot(
    modifier: Modifier = Modifier
) {
    // NOTE: This uses Google's *test* Ad Unit ID so the app runs out-of-the-box.
    // Replace with your own Ad Unit ID from your AdMob account when ready.
    val adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test MREC
    val context = LocalContext.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = {
                    AdView(context).apply {
                        setAdSize(AdSize.MEDIUM_RECTANGLE) // 300x250
                        setAdUnitId(adUnitId)
                        loadAd(AdRequest.Builder().build())
                    }
                },
                modifier = Modifier
                    .width(300.dp)
                    .height(250.dp)
            )
        }
    }
}

@Composable
private fun SavedTile(
    tile: Tile,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val src = remember { MutableInteractionSource() }
    Card(
        modifier = modifier
            .height(64.dp)
            .pressScale(src)
            .clickable(
                interactionSource = src,
                indication = rememberRipple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3F6))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                tile.icon()
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tile.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(tile.subtitle, fontSize = 12.sp, color = Color(0xFF777777), maxLines = 1)
            }
        }
    }
}

@Composable
private fun BigButton(
    text: String,
    onClick: () -> Unit
) {
    val src = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(src)
            .clickable(
                interactionSource = src,
                indication = rememberRipple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF9A9A9A))
        }
    }
}
