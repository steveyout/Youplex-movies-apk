package com.example.cinestream.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinestream.data.analytics.AnalyticsManager
import com.example.cinestream.data.api.TmdbRepository
import com.example.cinestream.data.local.*
import com.example.cinestream.data.model.*
import com.example.cinestream.data.provider.ProviderManager
import com.example.cinestream.data.update.UpdateChecker
import com.example.cinestream.data.update.UpdateInfo
import com.example.cinestream.ui.theme.AppThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = CineDatabase.getDatabase(application)
    private val dao = database.cineDao()
    private val tmdbRepo = TmdbRepository()
    private val prefs = application.getSharedPreferences("cinestream_prefs", android.content.Context.MODE_PRIVATE)

    // Preferences & Settings State (Defaults to SYSTEM for automatic theme detection)
    private val _themeMode = MutableStateFlow(
        try {
            val saved = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
            AppThemeMode.valueOf(saved)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _selectedServerId = MutableStateFlow(
        prefs.getString("selected_server_id", ProviderManager.DEFAULT_PROVIDER_ID) ?: ProviderManager.DEFAULT_PROVIDER_ID
    )
    val selectedServerId: StateFlow<String> = _selectedServerId.asStateFlow()

    private val _blockedAdsCount = MutableStateFlow(28)
    val blockedAdsCount: StateFlow<Int> = _blockedAdsCount.asStateFlow()

    // Version Update Checking State
    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    // Home Catalog & Genre States
    private val _trending = MutableStateFlow<List<MediaItem>>(emptyList())
    val trending: StateFlow<List<MediaItem>> = _trending.asStateFlow()

    private val _popularMovies = MutableStateFlow<List<MediaItem>>(emptyList())
    val popularMovies: StateFlow<List<MediaItem>> = _popularMovies.asStateFlow()

    private val _popularTv = MutableStateFlow<List<MediaItem>>(emptyList())
    val popularTv: StateFlow<List<MediaItem>> = _popularTv.asStateFlow()

    private val _topRated = MutableStateFlow<List<MediaItem>>(emptyList())
    val topRated: StateFlow<List<MediaItem>> = _topRated.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _selectedGenre = MutableStateFlow<Genre?>(null)
    val selectedGenre: StateFlow<Genre?> = _selectedGenre.asStateFlow()

    private val _genreMediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val genreMediaItems: StateFlow<List<MediaItem>> = _genreMediaItems.asStateFlow()

    private val _isLoadingGenreMedia = MutableStateFlow(false)
    val isLoadingGenreMedia: StateFlow<Boolean> = _isLoadingGenreMedia.asStateFlow()

    private val _isLoadingHome = MutableStateFlow(true)
    val isLoadingHome: StateFlow<Boolean> = _isLoadingHome.asStateFlow()

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<MediaItem>>(emptyList())
    val searchResults: StateFlow<List<MediaItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Media Detail & TV Episode State
    private val _selectedMedia = MutableStateFlow<MediaItem?>(null)
    val selectedMedia: StateFlow<MediaItem?> = _selectedMedia.asStateFlow()

    private val _selectedSeason = MutableStateFlow(1)
    val selectedSeason: StateFlow<Int> = _selectedSeason.asStateFlow()

    private val _selectedEpisode = MutableStateFlow(1)
    val selectedEpisode: StateFlow<Int> = _selectedEpisode.asStateFlow()

    private val _seasons = MutableStateFlow<List<TvSeason>>(emptyList())
    val seasons: StateFlow<List<TvSeason>> = _seasons.asStateFlow()

    private val _episodes = MutableStateFlow<List<TvEpisode>>(emptyList())
    val episodes: StateFlow<List<TvEpisode>> = _episodes.asStateFlow()

    // Room Persistent Flows
    val watchlist: StateFlow<List<WatchlistEntity>> = dao.getAllWatchlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<HistoryEntity>> = dao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadEntity>> = dao.getAllDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var searchJob: Job? = null

    init {
        loadHomeCatalog()
        seedSampleHistoryIfEmpty()
        checkForUpdates()
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            try {
                val currentCode = com.example.BuildConfig.VERSION_CODE
                val currentName = com.example.BuildConfig.VERSION_NAME
                val info = UpdateChecker.checkUpdate(currentVersionCode = currentCode, currentVersionName = currentName)
                AnalyticsManager.logAppUpdateCheck(info.latestVersionName, info.isUpdateRequired)
                if (info.isUpdateAvailable || info.isUpdateRequired) {
                    _updateInfo.value = info
                } else {
                    _updateInfo.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isCheckingUpdate.value = false
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
    }

    private fun seedSampleHistoryIfEmpty() {
        viewModelScope.launch {
            delay(500)
            val currentHistory = dao.getAllHistory().first()
            if (currentHistory.isEmpty()) {
                dao.insertOrUpdateHistory(
                    HistoryEntity(
                        id = "66732-tv-1-3",
                        tmdbId = "66732",
                        mediaType = "tv",
                        title = "Stranger Things",
                        posterPath = "/49WJfe161A310c140c1.jpg",
                        backdropPath = "/56223120x.jpg",
                        season = 1,
                        episode = 3,
                        progressSeconds = 1800,
                        totalDurationSeconds = 3000,
                        lastWatchedAt = System.currentTimeMillis() - 3600000
                    )
                )
                dao.insertOrUpdateHistory(
                    HistoryEntity(
                        id = "693134-movie-1-1",
                        tmdbId = "693134",
                        mediaType = "movie",
                        title = "Dune: Part Two",
                        posterPath = "/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                        backdropPath = "/xOMo8A2B2C23.jpg",
                        season = 1,
                        episode = 1,
                        progressSeconds = 4200,
                        totalDurationSeconds = 9900,
                        lastWatchedAt = System.currentTimeMillis() - 7200000
                    )
                )
            }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun setSelectedServerId(serverId: String) {
        _selectedServerId.value = serverId
        prefs.edit().putString("selected_server_id", serverId).apply()
        AnalyticsManager.logServerProviderChanged(serverId)
    }

    fun incrementBlockedAds() {
        _blockedAdsCount.value += 1
    }

    fun loadHomeCatalog() {
        viewModelScope.launch {
            _isLoadingHome.value = true
            try {
                val fetchedGenres = tmdbRepo.getMovieGenres()
                _genres.value = fetchedGenres
                _trending.value = tmdbRepo.getTrending()
                _popularMovies.value = tmdbRepo.getPopularMovies()
                _popularTv.value = tmdbRepo.getPopularTvShows()
                _topRated.value = tmdbRepo.getTopRated()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingHome.value = false
            }
        }
    }

    fun selectGenre(genre: Genre?) {
        _selectedGenre.value = genre
        if (genre == null) {
            _genreMediaItems.value = emptyList()
            _isLoadingGenreMedia.value = false
            return
        }

        viewModelScope.launch {
            _isLoadingGenreMedia.value = true
            try {
                val movies = tmdbRepo.getMediaByGenre(genre.id, MediaType.MOVIE)
                val tvShows = tmdbRepo.getMediaByGenre(genre.id, MediaType.TV)
                _genreMediaItems.value = (movies + tvShows).distinctBy { it.tmdbId }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingGenreMedia.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        AnalyticsManager.logSearch(query)
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(300) // Debounce
            _searchResults.value = tmdbRepo.searchMedia(query)
            _isSearching.value = false
        }
    }

    fun selectMedia(item: MediaItem) {
        _selectedMedia.value = item
        _selectedSeason.value = 1
        _selectedEpisode.value = 1
        AnalyticsManager.logMediaSelected(item.tmdbId, item.displayTitle, if (item.mediaType == MediaType.TV) "tv" else "movie")

        if (item.mediaType == MediaType.TV) {
            viewModelScope.launch {
                val seasonsList = tmdbRepo.getTvSeasons(item.tmdbId)
                _seasons.value = seasonsList
                val seasonNum = seasonsList.firstOrNull()?.seasonNumber ?: 1
                _selectedSeason.value = seasonNum
                _episodes.value = tmdbRepo.getTvEpisodes(item.tmdbId, seasonNum)
            }
        } else {
            _seasons.value = emptyList()
            _episodes.value = emptyList()
        }
    }

    fun selectSeason(seasonNumber: Int) {
        val currentMedia = _selectedMedia.value ?: return
        _selectedSeason.value = seasonNumber
        _selectedEpisode.value = 1
        viewModelScope.launch {
            _episodes.value = tmdbRepo.getTvEpisodes(currentMedia.tmdbId, seasonNumber)
        }
    }

    fun selectEpisode(episodeNumber: Int) {
        _selectedEpisode.value = episodeNumber
    }

    fun isMediaInWatchlist(tmdbId: String): Flow<Boolean> {
        return dao.isInWatchlist(tmdbId)
    }

    fun toggleWatchlist(item: MediaItem) {
        viewModelScope.launch {
            val exists = watchlist.value.any { it.tmdbId == item.tmdbId }
            if (exists) {
                dao.deleteFromWatchlist(item.tmdbId)
            } else {
                dao.insertWatchlist(
                    WatchlistEntity(
                        tmdbId = item.tmdbId,
                        mediaType = if (item.mediaType == MediaType.TV) "tv" else "movie",
                        title = item.displayTitle,
                        posterPath = item.posterPath,
                        backdropPath = item.backdropPath,
                        voteAverage = item.voteAverage,
                        releaseYear = item.releaseYear,
                        overview = item.overview
                    )
                )
            }
        }
    }

    fun saveWatchHistory(
        item: MediaItem,
        season: Int = 1,
        episode: Int = 1,
        progressSec: Long = 0,
        totalSec: Long = 0
    ) {
        viewModelScope.launch {
            val historyId = "${item.tmdbId}-${if (item.mediaType == MediaType.TV) "tv" else "movie"}-$season-$episode"
            dao.insertOrUpdateHistory(
                HistoryEntity(
                    id = historyId,
                    tmdbId = item.tmdbId,
                    mediaType = if (item.mediaType == MediaType.TV) "tv" else "movie",
                    title = item.displayTitle,
                    posterPath = item.posterPath,
                    backdropPath = item.backdropPath,
                    season = season,
                    episode = episode,
                    progressSeconds = progressSec,
                    totalDurationSeconds = if (totalSec > 0) totalSec else 7200,
                    lastWatchedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            dao.deleteHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            dao.clearHistory()
        }
    }

    // Offline Download Manager Simulation
    fun startOfflineDownload(item: MediaItem, season: Int = 1, episode: Int = 1) {
        viewModelScope.launch {
            val downloadId = "${item.tmdbId}-${if (item.mediaType == MediaType.TV) "tv" else "movie"}-$season-$episode"
            val existing = dao.getDownloadById(downloadId)
            if (existing != null && existing.status == "COMPLETED") {
                return@launch
            }

            // Create download entry
            var currentProgress = existing?.progressMb ?: 0.0
            val totalSize = 1450.0 // ~1.45 GB

            dao.insertOrUpdateDownload(
                DownloadEntity(
                    id = downloadId,
                    tmdbId = item.tmdbId,
                    mediaType = if (item.mediaType == MediaType.TV) "tv" else "movie",
                    title = item.displayTitle,
                    posterPath = item.posterPath,
                    season = season,
                    episode = episode,
                    progressMb = currentProgress,
                    totalMb = totalSize,
                    status = "DOWNLOADING"
                )
            )

            // Simulate active background download loop with progress steps
            while (currentProgress < totalSize) {
                delay(800)
                // Check if user paused or deleted
                val check = dao.getDownloadById(downloadId)
                if (check == null || check.status == "PAUSED") {
                    break
                }

                currentProgress += 180.0
                if (currentProgress >= totalSize) {
                    currentProgress = totalSize
                    dao.insertOrUpdateDownload(
                        check.copy(
                            progressMb = currentProgress,
                            status = "COMPLETED"
                        )
                    )
                } else {
                    dao.insertOrUpdateDownload(
                        check.copy(
                            progressMb = currentProgress,
                            status = "DOWNLOADING"
                        )
                    )
                }
            }
        }
    }

    fun togglePauseDownload(downloadId: String) {
        viewModelScope.launch {
            val d = dao.getDownloadById(downloadId) ?: return@launch
            if (d.status == "DOWNLOADING") {
                dao.insertOrUpdateDownload(d.copy(status = "PAUSED"))
            } else if (d.status == "PAUSED") {
                dao.insertOrUpdateDownload(d.copy(status = "DOWNLOADING"))
                // Resume download loop
                val item = MediaItem(
                    id = d.tmdbId.toIntOrNull() ?: 0,
                    tmdbId = d.tmdbId,
                    title = d.title,
                    posterPath = d.posterPath,
                    mediaType = if (d.mediaType == "tv") MediaType.TV else MediaType.MOVIE
                )
                startOfflineDownload(item, d.season, d.episode)
            }
        }
    }

    fun deleteDownload(downloadId: String) {
        viewModelScope.launch {
            dao.deleteDownload(downloadId)
        }
    }
}
