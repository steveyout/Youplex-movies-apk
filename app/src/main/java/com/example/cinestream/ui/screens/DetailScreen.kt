package com.example.cinestream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.cinestream.data.model.MediaItem
import com.example.cinestream.data.model.MediaType
import com.example.cinestream.data.provider.ProviderManager
import com.example.cinestream.ui.theme.CinemaGold
import com.example.cinestream.ui.theme.CinemaRed
import com.example.cinestream.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onPlayClick: (MediaItem, Int, Int, String) -> Unit,
    onSimilarMediaClick: (MediaItem) -> Unit
) {
    val selectedMedia by viewModel.selectedMedia.collectAsState()
    val media = selectedMedia ?: return

    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val selectedEpisode by viewModel.selectedEpisode.collectAsState()
    val seasons by viewModel.seasons.collectAsState()
    val episodes by viewModel.episodes.collectAsState()
    val selectedServerId by viewModel.selectedServerId.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val popularMovies by viewModel.popularMovies.collectAsState()

    val inWatchlist = watchlist.any { it.tmdbId == media.tmdbId }
    val downloadId = "${media.tmdbId}-${if (media.mediaType == MediaType.TV) "tv" else "movie"}-$selectedSeason-$selectedEpisode"
    val downloadItem = downloads.find { it.id == downloadId }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .testTag("detail_screen_scroll")
        ) {
            // Header Backdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(media.fullBackdropUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = media.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                // Large Central Play Button Overlay
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(CinemaRed)
                        .clickable { onPlayClick(media, selectedSeason, selectedEpisode, selectedServerId) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Now",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Media Info Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-30).dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Poster Card
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(media.fullPosterUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = media.displayTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(110.dp)
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, CinemaRed, RoundedCornerShape(12.dp))
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = CinemaRed,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (media.mediaType == MediaType.TV) "TV SERIES" else "FEATURE MOVIE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = media.displayTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = CinemaGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${String.format("%.1f", media.voteAverage)} Rating",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• ${media.releaseYear}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Play, Watchlist, Download
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onPlayClick(media, selectedSeason, selectedEpisode, selectedServerId) },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_play_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stream Now", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Watchlist Icon Toggle
                    OutlinedIconButton(
                        onClick = { viewModel.toggleWatchlist(media) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("detail_watchlist_toggle")
                    ) {
                        Icon(
                            imageVector = if (inWatchlist) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Watchlist",
                            tint = if (inWatchlist) CinemaRed else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Download Button
                    OutlinedIconButton(
                        onClick = {
                            viewModel.startOfflineDownload(media, selectedSeason, selectedEpisode)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("detail_download_button")
                    ) {
                        Icon(
                            imageVector = if (downloadItem?.status == "COMPLETED") Icons.Default.DownloadDone else Icons.Default.Download,
                            contentDescription = "Download",
                            tint = if (downloadItem?.status == "COMPLETED") Color(0xFF10B981) else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Download Progress Notification Banner if active
                if (downloadItem != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (downloadItem.status == "COMPLETED") "✅ Download Completed (1.45 GB)"
                                    else "⬇️ Downloading Offline Media... (${String.format("%.1f", downloadItem.progressMb)} / ${downloadItem.totalMb} MB)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (downloadItem.status != "COMPLETED") {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { (downloadItem.progressMb / downloadItem.totalMb).toFloat() },
                                        color = CinemaRed,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stream Server Provider Picker
                Text(
                    text = "🌐 Select Streaming Server (${ProviderManager.providers.size} Servers Available)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ProviderManager.providers) { server ->
                        FilterChip(
                            selected = selectedServerId == server.id,
                            onClick = { viewModel.setSelectedServerId(server.id) },
                            label = { Text(server.name, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // If TV Series: Seasons and Episodes Picker
                if (media.mediaType == MediaType.TV) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "📺 Seasons & Episodes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Season Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(seasons) { season ->
                            FilterChip(
                                selected = selectedSeason == season.seasonNumber,
                                onClick = { viewModel.selectSeason(season.seasonNumber) },
                                label = { Text(season.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CinemaRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Episodes Horizontal Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(episodes) { ep ->
                            val isEpSelected = selectedEpisode == ep.episodeNumber
                            Surface(
                                color = if (isEpSelected) CinemaRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = if (isEpSelected) ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(CinemaRed, CinemaRed))
                                ) else null,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .width(180.dp)
                                    .clickable { viewModel.selectEpisode(ep.episodeNumber) }
                                    .padding(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "E${ep.episodeNumber}: ${ep.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ep.overview,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Overview
                Text(
                    text = "Storyline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = media.overview.ifBlank { "No detailed synopsis available for this title." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Similar Recommendations
                Text(
                    text = "You Might Also Like",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(popularMovies.take(6)) { rec ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { onSimilarMediaClick(rec) }
                        ) {
                            Column {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(rec.fullPosterUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = rec.displayTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(170.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rec.displayTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Top Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .align(Alignment.TopStart)
                .testTag("detail_back_button")
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}
