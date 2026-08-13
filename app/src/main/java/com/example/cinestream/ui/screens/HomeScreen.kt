package com.example.cinestream.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
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
import com.example.cinestream.data.local.HistoryEntity
import com.example.cinestream.data.model.MediaItem
import com.example.cinestream.data.model.MediaType
import com.example.cinestream.ui.components.HeroCarousel
import com.example.cinestream.ui.components.MediaCard
import com.example.cinestream.ui.theme.AppThemeMode
import com.example.cinestream.ui.theme.CinemaRed
import com.example.cinestream.ui.viewmodel.MainViewModel

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import com.example.cinestream.data.model.Genre

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onMediaClick: (MediaItem) -> Unit,
    onPlayClick: (MediaItem) -> Unit,
    onContinueWatchingClick: (MediaItem, Int, Int) -> Unit,
    onSearchClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val trending by viewModel.trending.collectAsState()
    val popularMovies by viewModel.popularMovies.collectAsState()
    val popularTv by viewModel.popularTv.collectAsState()
    val topRated by viewModel.topRated.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isLoading by viewModel.isLoadingHome.collectAsState()

    val genres by viewModel.genres.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val genreMediaItems by viewModel.genreMediaItems.collectAsState()
    val isLoadingGenreMedia by viewModel.isLoadingGenreMedia.collectAsState()

    fun isInWatchlist(tmdbId: String): Boolean {
        return watchlist.any { it.tmdbId == tmdbId }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CinemaRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "YOUPLEX",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = CinemaRed,
                        letterSpacing = 1.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Search Button
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("home_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Quick Theme Toggle Button
                    IconButton(
                        onClick = {
                            val nextMode = if (themeMode == AppThemeMode.DARK) AppThemeMode.LIGHT else AppThemeMode.DARK
                            viewModel.setThemeMode(nextMode)
                        },
                        modifier = Modifier.testTag("home_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (themeMode == AppThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                com.example.cinestream.ui.components.HomeSkeletonScreen()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("home_scroll_list"),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Hero Carousel Banner
                item {
                    HeroCarousel(
                        items = trending,
                        onItemClick = onMediaClick,
                        onPlayClick = onPlayClick,
                        onWatchlistToggle = { viewModel.toggleWatchlist(it) },
                        isInWatchlist = { isInWatchlist(it) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }

                // TMDB Genre Filter Bar
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏷️ Explore Genres",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (selectedGenre != null) {
                                TextButton(onClick = { viewModel.selectGenre(null) }) {
                                    Text("Clear Filter", fontSize = 12.sp, color = CinemaRed)
                                }
                            }
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedGenre == null,
                                    onClick = { viewModel.selectGenre(null) },
                                    label = { Text("All", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CinemaRed,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                            items(genres) { genre ->
                                FilterChip(
                                    selected = selectedGenre?.id == genre.id,
                                    onClick = {
                                        if (selectedGenre?.id == genre.id) viewModel.selectGenre(null)
                                        else viewModel.selectGenre(genre)
                                    },
                                    label = { Text(genre.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CinemaRed,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                }

                if (selectedGenre != null) {
                    item {
                        if (isLoadingGenreMedia) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = CinemaRed)
                            }
                        } else if (genreMediaItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No titles found for ${selectedGenre?.name}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            MediaSectionRow(
                                title = "🎬 Top ${selectedGenre?.name} Titles",
                                items = genreMediaItems,
                                onMediaClick = onMediaClick,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    }
                }

                if (selectedGenre == null) {
                    // Section: Continue Watching
                    if (watchHistory.isNotEmpty()) {
                        item {
                            ContinueWatchingRow(
                                historyItems = watchHistory,
                                onHistoryClick = { history ->
                                    val item = MediaItem(
                                        id = history.tmdbId.toIntOrNull() ?: 0,
                                        tmdbId = history.tmdbId,
                                        title = history.title,
                                        posterPath = history.posterPath,
                                        backdropPath = history.backdropPath,
                                        mediaType = if (history.mediaType == "tv") MediaType.TV else MediaType.MOVIE
                                    )
                                    onContinueWatchingClick(item, history.season, history.episode)
                                },
                                onRemoveHistory = { historyId ->
                                    viewModel.deleteHistoryItem(historyId)
                                }
                            )
                        }
                    }

                    // Section 1: Popular Movies
                    item {
                        MediaSectionRow(
                            title = "🍿 Trending Movies",
                            items = popularMovies,
                            onMediaClick = onMediaClick,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }

                    // Section 2: Popular TV Series
                    item {
                        MediaSectionRow(
                            title = "📺 Popular TV Shows",
                            items = popularTv,
                            onMediaClick = onMediaClick,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }

                    // Section 3: Top Rated Masterpieces
                    item {
                        MediaSectionRow(
                            title = "⭐ Top Rated Cinema",
                            items = topRated,
                            onMediaClick = onMediaClick,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingRow(
    historyItems: List<HistoryEntity>,
    onHistoryClick: (HistoryEntity) -> Unit,
    onRemoveHistory: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("continue_watching_section")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "▶️ Continue Watching",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${historyItems.size} in progress",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(historyItems, key = { it.id }) { history ->
                ContinueWatchingCard(
                    history = history,
                    onClick = { onHistoryClick(history) },
                    onRemove = { onRemoveHistory(history.id) }
                )
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    history: HistoryEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val progressRatio = if (history.totalDurationSeconds > 0) {
        (history.progressSeconds.toFloat() / history.totalDurationSeconds.toFloat()).coerceIn(0.15f, 0.95f)
    } else {
        0.5f
    }

    val imageUrl = if (!history.backdropPath.isNullOrBlank()) {
        "https://image.tmdb.org/t/p/w500${history.backdropPath}"
    } else {
        "https://image.tmdb.org/t/p/w500${history.posterPath}"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
            .testTag("continue_watching_card_${history.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = history.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark Gradient at bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Play Button Center Overlay
                IconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                        .background(CinemaRed.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // TV Badge if applicable
                if (history.mediaType == "tv") {
                    Surface(
                        color = CinemaRed,
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "S${history.season}:E${history.episode}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Delete Button top right
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Progress Bar at bottom of thumbnail
                LinearProgressIndicator(
                    progress = { progressRatio },
                    color = CinemaRed,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter)
                )
            }

            // Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = history.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (history.mediaType == "tv") "Season ${history.season} Episode ${history.episode}" else "Movie • Resume watching",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MediaSectionRow(
    title: String,
    items: List<MediaItem>,
    onMediaClick: (MediaItem) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                MediaCard(
                    item = item,
                    onClick = { onMediaClick(item) },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }
        }
    }
}

