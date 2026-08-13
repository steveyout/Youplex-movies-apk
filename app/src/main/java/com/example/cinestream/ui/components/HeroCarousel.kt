package com.example.cinestream.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
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
import com.example.cinestream.ui.theme.CinemaGold
import com.example.cinestream.ui.theme.CinemaRed
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HeroCarousel(
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
    onPlayClick: (MediaItem) -> Unit,
    onWatchlistToggle: (MediaItem) -> Unit,
    isInWatchlist: (String) -> Boolean,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    if (items.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    // Auto rotate every 6 seconds
    LaunchedEffect(items) {
        while (true) {
            delay(6000)
            if (items.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % items.size
            }
        }
    }

    val currentItem = items.getOrNull(currentIndex) ?: return
    val inWatchlist = isInWatchlist(currentItem.tmdbId)

    val backdropModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                state = rememberSharedContentState(key = "media_backdrop_${currentItem.tmdbId}"),
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    } else Modifier

    val titleModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = "media_title_${currentItem.tmdbId}"),
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    } else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .clickable { onItemClick(currentItem) }
            .testTag("hero_carousel")
    ) {
        // Backdrop Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentItem.fullBackdropUrl)
                .crossfade(true)
                .build(),
            contentDescription = currentItem.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .then(backdropModifier)
        )

        // Vignette Gradient Overlays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        // Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Trending Badge
            Surface(
                color = CinemaRed,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "🔥 #1 TRENDING TODAY",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = currentItem.displayTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = titleModifier
            )

            // Rating & Meta info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = CinemaGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${String.format("%.1f", currentItem.voteAverage)} Rating",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• ${currentItem.releaseYear}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "4K Ultra HD",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Overview snippet
            Text(
                text = currentItem.overview,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            // Action Buttons Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onPlayClick(currentItem) },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .testTag("hero_play_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Watch Now", fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = { onWatchlistToggle(currentItem) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.6f)))
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .testTag("hero_watchlist_button")
                ) {
                    Icon(
                        imageVector = if (inWatchlist) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = "Watchlist",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (inWatchlist) "Saved" else "My List",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                items.take(5).forEachIndexed { index, item ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (index == currentIndex) 22.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex) CinemaRed
                                else Color.White.copy(alpha = 0.35f)
                            )
                            .clickable { currentIndex = index }
                    )
                }
            }
        }
    }
}
