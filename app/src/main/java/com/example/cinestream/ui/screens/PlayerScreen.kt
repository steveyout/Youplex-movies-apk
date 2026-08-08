package com.example.cinestream.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cinestream.data.adblock.AdBlocker
import com.example.cinestream.data.model.MediaItem
import com.example.cinestream.data.model.MediaType
import com.example.cinestream.data.provider.Provider
import com.example.cinestream.data.provider.ProviderManager
import com.example.cinestream.ui.components.AdBlockIndicator
import com.example.cinestream.ui.theme.CinemaRed
import com.example.cinestream.ui.viewmodel.MainViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(
    viewModel: MainViewModel,
    media: MediaItem,
    season: Int,
    episode: Int,
    serverId: String,
    onBackClick: () -> Unit
) {
    val blockedAdsCount by viewModel.blockedAdsCount.collectAsState()
    var currentServerId by remember { mutableStateOf(serverId) }
    var showServerPicker by remember { mutableStateOf(false) }
    var showQuickBar by remember { mutableStateOf(false) }
    var isLoadingPage by remember { mutableStateOf(true) }
    var hasPageError by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var customVideoView by remember { mutableStateOf<View?>(null) }

    val mediaTypeStr = if (media.mediaType == MediaType.TV) "tv" else "movie"
    val embedUrl = remember(media.tmdbId, mediaTypeStr, season, episode, currentServerId) {
        ProviderManager.getEmbedUrl(
            providerId = currentServerId,
            type = mediaTypeStr,
            tmdbId = media.tmdbId,
            season = season,
            episode = episode
        )
    }

    val currentProvider = remember(currentServerId) {
        ProviderManager.providers.find { it.id == currentServerId } ?: ProviderManager.providers.first()
    }

    // Save watch history on load
    LaunchedEffect(media, season, episode) {
        viewModel.saveWatchHistory(media, season, episode)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("player_screen_root")
    ) {
        // Embedded Ad-Blocked Android WebView Player
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewRef = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = true
                        allowContentAccess = true
                        javaScriptCanOpenWindowsAutomatically = false
                        setSupportMultipleWindows(true) // We override onCreateWindow to block popup windows!
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                    }

                    // JavaScript Bridge to count blocked popups
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onAdBlocked() {
                            viewModel.incrementBlockedAds()
                        }
                    }, "AndroidAdBlock")

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString()
                            if (AdBlocker.shouldBlockUrl(url)) {
                                viewModel.incrementBlockedAds()
                                return true // Intercept and block redirect/ad navigation
                            }
                            return false
                        }

                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val url = request?.url?.toString()
                            if (AdBlocker.shouldBlockUrl(url)) {
                                viewModel.incrementBlockedAds()
                                return WebResourceResponse(
                                    "text/plain",
                                    "UTF-8",
                                    java.io.ByteArrayInputStream(ByteArray(0))
                                )
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoadingPage = true
                            hasPageError = false
                            view?.evaluateJavascript(AdBlocker.adBlockJavaScript, null)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoadingPage = false
                            view?.evaluateJavascript(AdBlocker.adBlockJavaScript, null)
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                hasPageError = true
                                showQuickBar = true
                            }
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        // Strict popup blocker override
                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: android.os.Message?
                        ): Boolean {
                            viewModel.incrementBlockedAds()
                            return false // Refuse opening ad popup window!
                        }

                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            customVideoView = view
                        }

                        override fun onHideCustomView() {
                            customVideoView = null
                        }

                        override fun onPermissionRequest(request: PermissionRequest?) {
                            request?.grant(request.resources)
                        }
                    }

                    loadUrl(embedUrl)
                }
            },
            update = { webView ->
                if (webView.url != embedUrl) {
                    webView.loadUrl(embedUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // HTML5 Fullscreen Video Custom View Overlay
        customVideoView?.let { videoView ->
            AndroidView(
                factory = {
                    FrameLayout(it).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        addView(
                            videoView,
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Loading Overlay
        if (isLoadingPage) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CinemaRed)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Connecting to ${currentProvider.name}...",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Error Banner if current server fails to load
        if (hasPageError) {
            Surface(
                color = CinemaRed,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Unable to connect to ${currentProvider.name}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try switching to another server below",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showServerPicker = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Switch Stream Server", color = CinemaRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Floating Player Top Bar Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .testTag("player_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column {
                    Text(
                        text = media.displayTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    Text(
                        text = if (media.mediaType == MediaType.TV) "Season $season • Episode $episode" else "Feature Movie",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ad Blocker Shield Badge
                AdBlockIndicator(blockedCount = blockedAdsCount)

                // Server Dropdown Switcher
                ServerDropdownSelector(
                    currentServerId = currentServerId,
                    providers = ProviderManager.providers,
                    onServerSelected = { provider ->
                        currentServerId = provider.id
                        viewModel.setSelectedServerId(provider.id)
                    }
                )

                // Quick Bar Toggle
                IconButton(
                    onClick = { showQuickBar = !showQuickBar },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (showQuickBar) CinemaRed else Color.Black.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Quick Server Bar",
                        tint = Color.White
                    )
                }

                // Reload Player
                IconButton(
                    onClick = { webViewRef?.reload() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = Color.White
                    )
                }
            }
        }

        // Quick Bottom Server Switcher Bar
        AnimatedVisibility(
            visible = showQuickBar,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.88f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ Quick Switch Server",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        TextButton(onClick = { showServerPicker = true }) {
                            Text("All 12 Servers", color = CinemaRed, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ProviderManager.providers) { provider ->
                            val isSelected = currentServerId == provider.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    currentServerId = provider.id
                                    viewModel.setSelectedServerId(provider.id)
                                    showQuickBar = false
                                },
                                label = {
                                    Text(
                                        text = provider.name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CinemaRed,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Server Provider Dialog Sheet
        if (showServerPicker) {
            AlertDialog(
                onDismissRequest = { showServerPicker = false },
                title = {
                    Text(
                        text = "🌐 Select Streaming Server",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "If a movie or episode fails to play on your current server, tap any server below to switch instantly.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        ProviderManager.providers.forEach { provider ->
                            val isSelected = currentServerId == provider.id
                            Surface(
                                color = if (isSelected) CinemaRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentServerId = provider.id
                                        viewModel.setSelectedServerId(provider.id)
                                        showServerPicker = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = provider.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) CinemaRed else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Text("ACTIVE", color = CinemaRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showServerPicker = false }) {
                        Text("Close", color = CinemaRed)
                    }
                }
            )
        }
    }
}

@Composable
fun ServerDropdownSelector(
    currentServerId: String,
    providers: List<Provider>,
    onServerSelected: (Provider) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val currentProvider = remember(currentServerId, providers) {
        providers.find { it.id == currentServerId } ?: providers.firstOrNull() ?: Provider("vidlink", "Server 1", "", true)
    }

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.82f),
            border = BorderStroke(1.dp, CinemaRed.copy(alpha = 0.6f)),
            modifier = Modifier.testTag("server_dropdown_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = "Select Server",
                    tint = CinemaRed,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = currentProvider.name.split(" ")[0] + " " + currentProvider.name.split(" ").getOrElse(1) { "" },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand Server Dropdown",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color(0xFF18181C))
                .widthIn(min = 230.dp)
                .testTag("server_dropdown_menu")
        ) {
            Text(
                text = "🌐 SWITCH STREAM SERVER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = CinemaRed,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

            providers.forEach { provider ->
                val isSelected = provider.id == currentServerId
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = provider.name,
                                color = if (isSelected) CinemaRed else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = CinemaRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        onServerSelected(provider)
                    },
                    modifier = Modifier.background(if (isSelected) CinemaRed.copy(alpha = 0.18f) else Color.Transparent)
                )
            }
        }
    }
}

