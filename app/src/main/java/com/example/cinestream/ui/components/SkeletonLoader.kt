package com.example.cinestream.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1300,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 400f, translateAnim - 400f),
        end = Offset(translateAnim, translateAnim)
    )

    background(brush = brush)
}

@Composable
fun MediaCardSkeleton(
    modifier: Modifier = Modifier,
    cardWidth: Int = 140
) {
    Column(
        modifier = modifier
            .width(cardWidth.dp)
            .testTag("media_card_skeleton")
    ) {
        Box(
            modifier = Modifier
                .width(cardWidth.dp)
                .height((cardWidth * 1.5).dp)
                .clip(RoundedCornerShape(12.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .width((cardWidth * 0.85).dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width((cardWidth * 0.5).dp)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
    }
}

@Composable
fun HeroCarouselSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .shimmerEffect()
            .testTag("hero_carousel_skeleton")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Badge placeholder
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Title placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle placeholder
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons placeholder row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )

                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )
            }
        }
    }
}

@Composable
fun MediaSectionSkeleton(
    titleWidth: Int = 140,
    itemCount: Int = 5
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("media_section_skeleton")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(titleWidth.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(itemCount) {
                MediaCardSkeleton()
            }
        }
    }
}

@Composable
fun HomeSkeletonScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HeroCarouselSkeleton()
        MediaSectionSkeleton(titleWidth = 160)
        MediaSectionSkeleton(titleWidth = 140)
        MediaSectionSkeleton(titleWidth = 150)
    }
}

@Composable
fun ExploreGridSkeleton(
    itemCount: Int = 6
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("explore_grid_skeleton"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search bar skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Grid of skeletons
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(itemCount) {
                MediaCardSkeleton(cardWidth = 110)
            }
        }
    }
}
