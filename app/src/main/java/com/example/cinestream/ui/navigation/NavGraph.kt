package com.example.cinestream.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.cinestream.data.analytics.AnalyticsManager
import com.example.cinestream.ui.components.MandatoryUpdateDialog
import com.example.cinestream.ui.screens.*
import com.example.cinestream.ui.theme.CinemaRed
import com.example.cinestream.ui.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Explore : Screen("explore", "Explore", Icons.Default.Search)
    object Watchlist : Screen("watchlist", "My List", Icons.Default.Bookmark)
    object Downloads : Screen("downloads", "Downloads", Icons.Default.Download)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Detail : Screen("detail", "Detail", Icons.Default.Home)
    object Player : Screen("player", "Player", Icons.Default.Home)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainNavGraph(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val updateInfo by viewModel.updateInfo.collectAsState()

    // Track Screen View Analytics
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            AnalyticsManager.logScreenView(route)
        }
    }

    // Mandatory Update Overlay
    updateInfo?.let { info ->
        MandatoryUpdateDialog(
            updateInfo = info,
            onDismissRequest = { viewModel.dismissUpdateDialog() }
        )
    }

    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Explore.route,
        Screen.Watchlist.route,
        Screen.Downloads.route,
        Screen.Settings.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    val bottomItems = listOf(
                        Screen.Home,
                        Screen.Explore,
                        Screen.Watchlist,
                        Screen.Downloads,
                        Screen.Settings
                    )

                    bottomItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CinemaRed,
                                selectedTextColor = CinemaRed,
                                indicatorColor = CinemaRed.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onMediaClick = { item ->
                            viewModel.selectMedia(item)
                            navController.navigate(Screen.Detail.route)
                        },
                        onPlayClick = { item ->
                            viewModel.selectMedia(item)
                            navController.navigate(Screen.Player.route)
                        },
                        onContinueWatchingClick = { item, season, episode ->
                            viewModel.selectMedia(item)
                            viewModel.selectSeason(season)
                            viewModel.selectEpisode(episode)
                            navController.navigate(Screen.Player.route)
                        },
                        onSearchClick = {
                            navController.navigate(Screen.Explore.route)
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable
                    )
                }

                composable(Screen.Explore.route) {
                    ExploreScreen(
                        viewModel = viewModel,
                        onMediaClick = { item ->
                            viewModel.selectMedia(item)
                            navController.navigate(Screen.Detail.route)
                        }
                    )
                }

                composable(Screen.Detail.route) {
                    DetailScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onPlayClick = { media, season, episode, serverId ->
                            navController.navigate(Screen.Player.route)
                        },
                        onSimilarMediaClick = { item ->
                            viewModel.selectMedia(item)
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable
                    )
                }

            composable(Screen.Player.route) {
                val selectedMedia by viewModel.selectedMedia.collectAsState()
                val selectedSeason by viewModel.selectedSeason.collectAsState()
                val selectedEpisode by viewModel.selectedEpisode.collectAsState()
                val selectedServerId by viewModel.selectedServerId.collectAsState()

                selectedMedia?.let { media ->
                    PlayerScreen(
                        viewModel = viewModel,
                        media = media,
                        season = selectedSeason,
                        episode = selectedEpisode,
                        serverId = selectedServerId,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Watchlist.route) {
                WatchlistScreen(
                    viewModel = viewModel,
                    onMediaClick = { item ->
                        viewModel.selectMedia(item)
                        navController.navigate(Screen.Detail.route)
                    },
                    onPlayHistoryClick = { item, season, episode ->
                        viewModel.selectMedia(item)
                        viewModel.selectSeason(season)
                        viewModel.selectEpisode(episode)
                        navController.navigate(Screen.Player.route)
                    }
                )
            }

            composable(Screen.Downloads.route) {
                DownloadsScreen(
                    viewModel = viewModel,
                    onPlayOfflineClick = { item, season, episode ->
                        viewModel.selectMedia(item)
                        viewModel.selectSeason(season)
                        viewModel.selectEpisode(episode)
                        navController.navigate(Screen.Player.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
}
