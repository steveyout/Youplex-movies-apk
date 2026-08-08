package com.example.cinestream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
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
import com.example.cinestream.data.local.HistoryEntity
import com.example.cinestream.data.local.WatchlistEntity
import com.example.cinestream.data.model.MediaItem
import com.example.cinestream.data.model.MediaType
import com.example.cinestream.ui.theme.CinemaGold
import com.example.cinestream.ui.theme.CinemaRed
import com.example.cinestream.ui.viewmodel.MainViewModel

@Composable
fun WatchlistScreen(
    viewModel: MainViewModel,
    onMediaClick: (MediaItem) -> Unit,
    onPlayHistoryClick: (MediaItem, Int, Int) -> Unit
) {
    val watchlist by viewModel.watchlist.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Watchlist (${watchlist.size})", "Continue Watching (${watchHistory.size})")

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
                    text = "Saved Content & History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = CinemaRed
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.Bold) }
                        )
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
            if (selectedTab == 0) {
                // Watchlist Tab
                if (watchlist.isEmpty()) {
                    EmptyState(
                        title = "Your Watchlist is Empty",
                        subtitle = "Explore movies and TV shows to save titles to watch later."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("watchlist_list")
                    ) {
                        items(watchlist) { item ->
                            WatchlistItemRow(
                                entity = item,
                                onClick = {
                                    onMediaClick(
                                        MediaItem(
                                            id = item.tmdbId.toIntOrNull() ?: 0,
                                            tmdbId = item.tmdbId,
                                            title = item.title,
                                            posterPath = item.posterPath,
                                            backdropPath = item.backdropPath,
                                            voteAverage = item.voteAverage,
                                            overview = item.overview,
                                            mediaType = if (item.mediaType == "tv") MediaType.TV else MediaType.MOVIE
                                        )
                                    )
                                },
                                onDelete = { viewModel.toggleWatchlist(
                                    MediaItem(
                                        id = item.tmdbId.toIntOrNull() ?: 0,
                                        tmdbId = item.tmdbId,
                                        title = item.title
                                    )
                                ) }
                            )
                        }
                    }
                }
            } else {
                // History Tab
                if (watchHistory.isEmpty()) {
                    EmptyState(
                        title = "No Watch History Yet",
                        subtitle = "Shows and movies you stream will automatically appear here."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("history_list")
                    ) {
                        items(watchHistory) { history ->
                            HistoryItemRow(
                                history = history,
                                onPlayClick = {
                                    val item = MediaItem(
                                        id = history.tmdbId.toIntOrNull() ?: 0,
                                        tmdbId = history.tmdbId,
                                        title = history.title,
                                        posterPath = history.posterPath,
                                        backdropPath = history.backdropPath,
                                        mediaType = if (history.mediaType == "tv") MediaType.TV else MediaType.MOVIE
                                    )
                                    onPlayHistoryClick(item, history.season, history.episode)
                                },
                                onDelete = { viewModel.deleteHistoryItem(history.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WatchlistItemRow(
    entity: WatchlistEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://image.tmdb.org/t/p/w200${entity.posterPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = entity.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(60.dp)
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = CinemaRed,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (entity.mediaType == "tv") "TV" else "MOVIE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = entity.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${String.format("%.1f", entity.voteAverage)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun HistoryItemRow(
    history: HistoryEntity,
    onPlayClick: () -> Unit,
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
                    .data("https://image.tmdb.org/t/p/w200${history.posterPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = history.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(60.dp)
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = history.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (history.mediaType == "tv") {
                    Text(
                        text = "Season ${history.season} • Episode ${history.episode}",
                        fontSize = 12.sp,
                        color = CinemaRed,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { 0.75f },
                    color = CinemaRed,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }

            IconButton(onClick = onPlayClick) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = CinemaRed)
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📁 $title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
