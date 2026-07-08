package com.example.ui

import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

import com.example.data.History
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.clickable

import androidx.webkit.ProfileStore
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    spaceId: Int,
    history: List<History>,
    onNavigateBack: () -> Unit,
    onDownloadRequested: (String, String) -> Unit,
    onHistoryUpdate: (String, String) -> Unit,
    currentSummary: String? = null,
    onSummarizeRequested: (String) -> Unit = {},
    onClearSummary: () -> Unit = {}
) {
    var urlInput by remember { mutableStateOf("https://www.google.com") }
    var currentUrl by remember { mutableStateOf("https://www.google.com") }
    var webView: WebView? by remember { mutableStateOf(null) }
    var originalUserAgent by remember { mutableStateOf<String?>(null) }
    var isDesktopMode by remember { mutableStateOf(false) }
    var isAdBlockerEnabled by remember { mutableStateOf(true) }
    var isCookiesEnabled by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showMediaSelector by remember { mutableStateOf(false) }
    var foundMedia by remember { mutableStateOf(emptyList<String>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Spaces")
            }
            IconButton(onClick = { webView?.goBack() }, enabled = webView?.canGoBack() == true) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
            }
            IconButton(onClick = { webView?.goForward() }, enabled = webView?.canGoForward() == true) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Go Forward")
            }
            IconButton(onClick = { webView?.reload() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
            
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp)),
                placeholder = { Text("URL") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = {
                        var target = urlInput
                        if (!target.startsWith("http://") && !target.startsWith("https://")) {
                            target = "https://$target"
                        }
                        currentUrl = target
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(25.dp)
            )

            IconButton(onClick = { showHistory = true }) {
                Icon(Icons.Default.History, contentDescription = "History")
            }
            IconButton(onClick = { 
                webView?.evaluateJavascript("(function() { return document.body.innerText; })();") { result ->
                    val text = result?.trim('"', '\'', ' ') ?: ""
                    onSummarizeRequested(text)
                }
            }) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Summarize", tint = MaterialTheme.colorScheme.primary)
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isDesktopMode) "Mobile Site" else "Desktop Site") },
                        onClick = {
                            isDesktopMode = !isDesktopMode
                            showMenu = false
                            webView?.let { wv ->
                                wv.settings.userAgentString = if (isDesktopMode) {
                                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
                                } else {
                                    originalUserAgent
                                }
                                wv.settings.loadWithOverviewMode = isDesktopMode
                                wv.settings.useWideViewPort = isDesktopMode
                                wv.reload()
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isAdBlockerEnabled) "Disable Ad Blocker" else "Enable Ad Blocker") },
                        onClick = {
                            isAdBlockerEnabled = !isAdBlockerEnabled
                            showMenu = false
                            webView?.reload()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isCookiesEnabled) "Block Cookies" else "Allow Cookies") },
                        onClick = {
                            isCookiesEnabled = !isCookiesEnabled
                            showMenu = false
                            webView?.let { wv ->
                                android.webkit.CookieManager.getInstance().setAcceptCookie(isCookiesEnabled)
                                android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(wv, isCookiesEnabled)
                                wv.reload()
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear Cache") },
                        onClick = {
                            webView?.clearCache(true)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("New Tab (Clear)") },
                        onClick = {
                            currentUrl = "https://www.google.com"
                            urlInput = currentUrl
                            webView?.clearHistory()
                            showMenu = false
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        if (WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) {
                            val profileStore = ProfileStore.getInstance()
                            val profile = profileStore.getOrCreateProfile("space_$spaceId")
                            WebViewCompat.setProfile(this, profile.name)
                        }

                        android.webkit.CookieManager.getInstance().setAcceptCookie(isCookiesEnabled)
                        android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, isCookiesEnabled)

                        if (originalUserAgent == null) {
                            originalUserAgent = settings.userAgentString
                        }

                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): android.webkit.WebResourceResponse? {
                                if (isAdBlockerEnabled) {
                                    val url = request?.url.toString().lowercase()
                                    if (url.contains("ads") || url.contains("track") || url.contains("analytics") || url.contains("banner") || url.contains("pop")) {
                                        return android.webkit.WebResourceResponse("text/plain", "UTF-8", java.io.ByteArrayInputStream(ByteArray(0)))
                                    }
                                }
                                return super.shouldInterceptRequest(view, request)
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                return false
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                url?.let { 
                                    urlInput = it 
                                    onHistoryUpdate(it, view?.title ?: "Unknown Title")
                                }
                            }
                        }
                        loadUrl(currentUrl)
                        webView = this
                    }
                },
                update = { view ->
                    if (view.url != currentUrl) {
                        view.loadUrl(currentUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Enhanced Floating Download Button
            ExtendedFloatingActionButton(
                onClick = {
                    val script = """
                        (function() {
                            var mediaUrls = new Set();
                            document.querySelectorAll('video source, video').forEach(v => { if (v.src) mediaUrls.add(v.src); });
                            document.querySelectorAll('audio source, audio').forEach(a => { if (a.src) mediaUrls.add(a.src); });
                            document.querySelectorAll('img').forEach(i => { if (i.src) mediaUrls.add(i.src); });
                            document.querySelectorAll('a').forEach(a => {
                                var href = a.href;
                                if (href && href.match(/\.(mp4|webm|avi|mov|mkv|mp3|wav|ogg|flac|m4a|jpg|jpeg|png|gif|webp)$/i)) {
                                    mediaUrls.add(href);
                                }
                            });
                            return Array.from(mediaUrls);
                        })();
                    """.trimIndent()
                    webView?.evaluateJavascript(script) { result ->
                        try {
                            val cleanResult = if (result == "null" || result == null) "[]" else result
                            val jsonArray = org.json.JSONArray(cleanResult)
                            val urls = mutableListOf<String>()
                            for (i in 0 until jsonArray.length()) {
                                urls.add(jsonArray.getString(i))
                            }
                            foundMedia = urls
                            showMediaSelector = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            foundMedia = emptyList()
                            showMediaSelector = true
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("download_fab"),
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Download Video")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download")
            }
            
            if (showMediaSelector) {
                ModalBottomSheet(onDismissRequest = { showMediaSelector = false }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Found Media",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        if (foundMedia.isEmpty()) {
                            Text(
                                "No downloadable media found on this page.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn {
                                items(foundMedia) { url ->
                                    val fileName = url.substringAfterLast('/', "downloaded_file_${System.currentTimeMillis()}")
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onDownloadRequested(url, fileName)
                                                showMediaSelector = false
                                            }
                                            .padding(vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = fileName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = url,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1
                                        )
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
            
            if (showHistory) {
                ModalBottomSheet(onDismissRequest = { showHistory = false }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        if (history.isEmpty()) {
                            Text(
                                "No history yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn {
                                items(history) { item ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                currentUrl = item.url
                                                showHistory = false
                                            }
                                            .padding(vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = item.url,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1
                                        )
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
            
            if (currentSummary != null) {
                AlertDialog(
                    onDismissRequest = onClearSummary,
                    title = { Text("AI Page Summary", style = MaterialTheme.typography.titleLarge) },
                    text = { 
                        LazyColumn {
                            item { Text(currentSummary) }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = onClearSummary) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}
