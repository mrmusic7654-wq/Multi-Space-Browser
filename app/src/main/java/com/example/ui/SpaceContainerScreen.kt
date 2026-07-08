package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.Download
import com.example.data.History

@Composable
fun SpaceContainerScreen(
    spaceName: String,
    spaceId: Int,
    downloads: List<Download>,
    history: List<History>,
    onNavigateBack: () -> Unit,
    onDownloadRequested: (String, String) -> Unit,
    onHistoryUpdate: (String, String) -> Unit,
    currentSummary: String? = null,
    onSummarizeRequested: (String) -> Unit = {},
    onClearSummary: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var currentVideoUrl by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Browser") },
                    label = { Text("Browser") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Download, contentDescription = "Progress") },
                    label = { Text("Progress") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Files") },
                    label = { Text("Files") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> {
                    BrowserScreen(
                        spaceId = spaceId,
                        history = history,
                        onNavigateBack = onNavigateBack,
                        onDownloadRequested = onDownloadRequested,
                        onHistoryUpdate = onHistoryUpdate,
                        currentSummary = currentSummary,
                        onSummarizeRequested = onSummarizeRequested,
                        onClearSummary = onClearSummary
                    )
                }
                1 -> {
                    // Progress Tab
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No active downloads", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                2 -> {
                    // Files / Player Tab
                    if (currentVideoUrl == null) {
                        DownloadsScreen(
                            downloads = downloads.filter { it.spaceId == spaceId },
                            onNavigateBack = onNavigateBack,
                            onPlayVideo = { url ->
                                currentVideoUrl = url
                            }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            VideoPlayerScreen(videoUrl = currentVideoUrl)
                            IconButton(
                                onClick = { currentVideoUrl = null },
                                modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = androidx.compose.foundation.shape.CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close Player")
                            }
                        }
                    }
                }
            }
        }
    }
}
