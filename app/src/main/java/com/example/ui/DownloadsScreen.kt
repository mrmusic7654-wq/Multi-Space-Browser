package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Download
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    downloads: List<Download>,
    onNavigateBack: () -> Unit,
    onPlayVideo: (String) -> Unit
) {
    var selectedCategory by remember { mutableIntStateOf(0) }
    val categories = listOf("All", "Videos", "Audio", "Images", "Other")

    val filteredDownloads = remember(downloads, selectedCategory) {
        when (selectedCategory) {
            1 -> downloads.filter { it.isVideo() }
            2 -> downloads.filter { it.isAudio() }
            3 -> downloads.filter { it.isImage() }
            4 -> downloads.filter { !it.isVideo() && !it.isAudio() && !it.isImage() }
            else -> downloads
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedCategory,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                divider = {}
            ) {
                categories.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedCategory == index,
                        onClick = { selectedCategory = index },
                        text = { Text(title) }
                    )
                }
            }

            if (filteredDownloads.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No files found.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDownloads) { download ->
                        DownloadItemCard(
                            download = download,
                            onClick = { onPlayVideo(download.url) }
                        )
                    }
                }
            }
        }
    }
}

fun Download.isVideo(): Boolean {
    val ext = this.fileName.substringAfterLast('.', "").lowercase()
    return ext in listOf("mp4", "mkv", "webm", "avi", "mov", "flv")
}

fun Download.isAudio(): Boolean {
    val ext = this.fileName.substringAfterLast('.', "").lowercase()
    return ext in listOf("mp3", "wav", "ogg", "m4a", "flac")
}

fun Download.isImage(): Boolean {
    val ext = this.fileName.substringAfterLast('.', "").lowercase()
    return ext in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
}

@Composable
fun DownloadItemCard(download: Download, onClick: () -> Unit) {
    val icon = when {
        download.isVideo() -> Icons.Default.PlayArrow
        download.isAudio() -> Icons.Default.AudioFile
        download.isImage() -> Icons.Default.Image
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 8.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "File Icon",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = download.fileName, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val date = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(download.timestamp))
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
