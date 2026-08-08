package com.example.cinestream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cinestream.data.local.DownloadEntity
import com.example.cinestream.data.model.MediaItem
import com.example.cinestream.data.model.MediaType
import com.example.cinestream.ui.theme.CinemaRed
import com.example.cinestream.ui.viewmodel.MainViewModel

@Composable
fun DownloadsScreen(
    viewModel: MainViewModel,
    onPlayOfflineClick: (MediaItem, Int, Int) -> Unit
) {
    val downloads by viewModel.downloads.collectAsState()

    val totalDownloadedGb = remember(downloads) {
        val sumMb = downloads.filter { it.status == "COMPLETED" }.sumOf { it.totalMb }
        sumMb / 1024.0
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Offline Downloads",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Storage usage header card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SdStorage,
                            contentDescription = "Storage",
                            tint = CinemaRed,
                            modifier = Modifier.size(28.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offline Storage Used",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${String.format("%.2f", totalDownloadedGb)} GB of 64 GB used",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (totalDownloadedGb / 64.0).toFloat().coerceIn(0f, 1f) },
                                color = CinemaRed,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (downloads.isEmpty()) {
                EmptyState(
                    title = "No Offline Downloads",
                    subtitle = "Tap the Download icon on any movie or episode page to watch offline without internet!"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("downloads_list")
                ) {
                    items(downloads) { d ->
                        DownloadItemRow(
                            download = d,
                            onPlayOffline = {
                                val item = MediaItem(
                                    id = d.tmdbId.toIntOrNull() ?: 0,
                                    tmdbId = d.tmdbId,
                                    title = d.title,
                                    posterPath = d.posterPath,
                                    mediaType = if (d.mediaType == "tv") MediaType.TV else MediaType.MOVIE
                                )
                                onPlayOfflineClick(item, d.season, d.episode)
                            },
                            onPauseToggle = { viewModel.togglePauseDownload(d.id) },
                            onDelete = { viewModel.deleteDownload(d.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadItemRow(
    download: DownloadEntity,
    onPlayOffline: () -> Unit,
    onPauseToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://image.tmdb.org/t/p/w200${download.posterPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = download.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(60.dp)
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = download.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (download.mediaType == "tv") {
                    Text(
                        text = "Season ${download.season} • Episode ${download.episode}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (download.status == "COMPLETED") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Downloaded (1.45 GB)", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "${download.status}: ${String.format("%.1f", download.progressMb)} / ${download.totalMb} MB",
                        fontSize = 11.sp,
                        color = CinemaRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (download.progressMb / download.totalMb).toFloat().coerceIn(0f, 1f) },
                        color = CinemaRed,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }

            if (download.status == "COMPLETED") {
                IconButton(onClick = onPlayOffline) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Offline", tint = CinemaRed)
                }
            } else {
                IconButton(onClick = onPauseToggle) {
                    Icon(
                        imageVector = if (download.status == "DOWNLOADING") Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Pause",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
