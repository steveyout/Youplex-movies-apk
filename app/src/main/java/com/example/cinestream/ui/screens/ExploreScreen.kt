package com.example.cinestream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.ExperimentalSharedTransitionApi
import com.example.cinestream.data.model.MediaItem
import com.example.cinestream.data.model.MediaType
import com.example.cinestream.ui.components.ExploreGridSkeleton
import com.example.cinestream.ui.components.MediaCard
import com.example.cinestream.ui.theme.CinemaRed
import com.example.cinestream.ui.viewmodel.MainViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ExploreScreen(
    viewModel: MainViewModel,
    onMediaClick: (MediaItem) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val trending by viewModel.trending.collectAsState()
    val popularMovies by viewModel.popularMovies.collectAsState()
    val popularTv by viewModel.popularTv.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val genres = listOf("ALL", "Movies", "TV Shows", "Action", "Sci-Fi", "Comedy", "Drama", "Horror")
    val popularSuggestions = listOf("Batman", "Avengers", "Stranger Things", "Inception", "Spider-Man", "Breaking Bad", "Avatar")

    val displayList = remember(searchQuery, searchResults, selectedFilter, trending, popularMovies, popularTv) {
        val base = if (searchQuery.isNotBlank()) searchResults else (trending + popularMovies + popularTv).distinctBy { it.tmdbId }
        when (selectedFilter) {
            "Movies" -> base.filter { it.mediaType == MediaType.MOVIE }
            "TV Shows" -> base.filter { it.mediaType == MediaType.TV }
            "Action", "Sci-Fi", "Comedy", "Drama", "Horror" -> base.filter {
                it.overview.contains(selectedFilter, ignoreCase = true) || it.displayTitle.contains(selectedFilter, ignoreCase = true)
            }
            else -> base
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Explore & Search",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search TextField with IME Action Search and auto-keyboard hide
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search movies, TV shows by title...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = CinemaRed
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.onSearchQueryChanged("")
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaRed,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Popular Search Suggestions Chips (quick fill search query)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(popularSuggestions) { suggestion ->
                        SuggestionChip(
                            onClick = {
                                viewModel.onSearchQueryChanged(suggestion)
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            },
                            label = {
                                Text(
                                    text = suggestion,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = CinemaRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(genres) { genre ->
                        FilterChip(
                            selected = selectedFilter == genre,
                            onClick = { selectedFilter = genre },
                            label = { Text(genre, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaRed,
                                selectedLabelColor = Color.White
                            )
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
            if (isSearching) {
                ExploreGridSkeleton()
            } else if (displayList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🔍 No Titles Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try searching for popular titles like 'Avengers', 'Inception', or 'Stranger Things'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.onSearchQueryChanged("Avengers")
                            focusManager.clearFocus()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaRed)
                    ) {
                        Text("Search 'Avengers'")
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (searchQuery.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Found ${displayList.size} titles for \"$searchQuery\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CinemaRed
                            )
                            TextButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Text("Clear", fontSize = 12.sp)
                            }
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("explore_grid")
                    ) {
                        items(displayList) { item ->
                            MediaCard(
                                item = item,
                                onClick = { onMediaClick(item) },
                                cardWidth = 160
                            )
                        }
                    }
                }
            }
        }
    }
}

