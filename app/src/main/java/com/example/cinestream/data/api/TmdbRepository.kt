package com.example.cinestream.data.api

import com.example.BuildConfig
import com.example.cinestream.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class TmdbRepository {

    // Configured TMDB API key with fallback
    private val tmdbApiKey: String
        get() {
            val envKey = try { BuildConfig.TMDB_API_KEY } catch (e: Exception) { "" }
            return if (!envKey.isNullOrBlank() && !envKey.contains("YOUR_")) envKey else "addfba41d0cb5aba2ebaae12ac92b671"
        }

    suspend fun getTrending(): List<MediaItem> = withContext(Dispatchers.IO) {
        val fetched = fetchFromTmdb("https://api.themoviedb.org/3/trending/all/day?api_key=$tmdbApiKey")
        if (fetched.isNotEmpty()) fetched else getFallbackTrending()
    }

    suspend fun getPopularMovies(): List<MediaItem> = withContext(Dispatchers.IO) {
        val fetched = fetchFromTmdb("https://api.themoviedb.org/3/movie/popular?api_key=$tmdbApiKey")
        if (fetched.isNotEmpty()) fetched else getFallbackMovies()
    }

    suspend fun getPopularTvShows(): List<MediaItem> = withContext(Dispatchers.IO) {
        val fetched = fetchFromTmdb("https://api.themoviedb.org/3/tv/popular?api_key=$tmdbApiKey", forceType = MediaType.TV)
        if (fetched.isNotEmpty()) fetched else getFallbackTvShows()
    }

    suspend fun getTopRated(): List<MediaItem> = withContext(Dispatchers.IO) {
        val fetched = fetchFromTmdb("https://api.themoviedb.org/3/movie/top_rated?api_key=$tmdbApiKey")
        if (fetched.isNotEmpty()) fetched else getFallbackTopRated()
    }

    suspend fun searchMedia(query: String): List<MediaItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        val fetched = fetchFromTmdb("https://api.themoviedb.org/3/search/multi?api_key=$tmdbApiKey&query=$encoded")
        if (fetched.isNotEmpty()) {
            fetched
        } else {
            val movieResults = fetchFromTmdb("https://api.themoviedb.org/3/search/movie?api_key=$tmdbApiKey&query=$encoded")
            val tvResults = fetchFromTmdb("https://api.themoviedb.org/3/search/tv?api_key=$tmdbApiKey&query=$encoded", forceType = MediaType.TV)
            val combined = (movieResults + tvResults).distinctBy { it.tmdbId }
            if (combined.isNotEmpty()) combined
            else (getFallbackMovies() + getFallbackTvShows()).filter {
                it.title.contains(query, ignoreCase = true) || it.overview.contains(query, ignoreCase = true)
            }
        }
    }

    suspend fun getTvSeasons(tmdbId: String): List<TvSeason> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.themoviedb.org/3/tv/$tmdbId?api_key=$tmdbApiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)
                val seasonsArr = json.optJSONArray("seasons") ?: return@withContext defaultSeasons()
                val list = mutableListOf<TvSeason>()
                for (i in 0 until seasonsArr.length()) {
                    val s = seasonsArr.getJSONObject(i)
                    val seasonNum = s.optInt("season_number", 1)
                    if (seasonNum > 0) {
                        list.add(
                            TvSeason(
                                id = s.optInt("id", i),
                                seasonNumber = seasonNum,
                                name = s.optString("name", "Season $seasonNum"),
                                episodeCount = s.optInt("episode_count", 10),
                                posterPath = s.optString("poster_path", null)
                            )
                        )
                    }
                }
                if (list.isNotEmpty()) return@withContext list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext defaultSeasons()
    }

    suspend fun getTvEpisodes(tmdbId: String, seasonNumber: Int): List<TvEpisode> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.themoviedb.org/3/tv/$tmdbId/season/$seasonNumber?api_key=$tmdbApiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)
                val epArr = json.optJSONArray("episodes") ?: return@withContext defaultEpisodes(seasonNumber)
                val list = mutableListOf<TvEpisode>()
                for (i in 0 until epArr.length()) {
                    val ep = epArr.getJSONObject(i)
                    list.add(
                        TvEpisode(
                            id = ep.optInt("id", i),
                            episodeNumber = ep.optInt("episode_number", i + 1),
                            seasonNumber = seasonNumber,
                            name = ep.optString("name", "Episode ${i + 1}"),
                            overview = ep.optString("overview", "Overview unavailable."),
                            stillPath = ep.optString("still_path", null),
                            voteAverage = ep.optDouble("vote_average", 8.2)
                        )
                    )
                }
                if (list.isNotEmpty()) return@withContext list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext defaultEpisodes(seasonNumber)
    }

    suspend fun getMovieGenres(): List<Genre> = withContext(Dispatchers.IO) {
        val fetched = fetchGenres("https://api.themoviedb.org/3/genre/movie/list?api_key=$tmdbApiKey")
        if (fetched.isNotEmpty()) fetched else defaultGenres()
    }

    suspend fun getTvGenres(): List<Genre> = withContext(Dispatchers.IO) {
        val fetched = fetchGenres("https://api.themoviedb.org/3/genre/tv/list?api_key=$tmdbApiKey")
        if (fetched.isNotEmpty()) fetched else defaultGenres()
    }

    suspend fun getMediaByGenre(genreId: Int, mediaType: MediaType = MediaType.MOVIE): List<MediaItem> = withContext(Dispatchers.IO) {
        val typeStr = if (mediaType == MediaType.TV) "tv" else "movie"
        val fetched = fetchFromTmdb("https://api.themoviedb.org/3/discover/$typeStr?api_key=$tmdbApiKey&with_genres=$genreId&sort_by=popularity.desc", forceType = mediaType)
        if (fetched.isNotEmpty()) fetched else (getFallbackMovies() + getFallbackTvShows()).filter { it.mediaType == mediaType }
    }

    private fun fetchGenres(urlString: String): List<Genre> {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)
                val genresArr = json.optJSONArray("genres") ?: return emptyList()
                val list = mutableListOf<Genre>()
                for (i in 0 until genresArr.length()) {
                    val g = genresArr.getJSONObject(i)
                    val id = g.optInt("id")
                    val name = g.optString("name")
                    if (id > 0 && name.isNotBlank()) {
                        list.add(Genre(id, name))
                    }
                }
                return list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    private fun defaultGenres(): List<Genre> = listOf(
        Genre(28, "Action"),
        Genre(12, "Adventure"),
        Genre(16, "Animation"),
        Genre(35, "Comedy"),
        Genre(80, "Crime"),
        Genre(99, "Documentary"),
        Genre(18, "Drama"),
        Genre(10751, "Family"),
        Genre(14, "Fantasy"),
        Genre(36, "History"),
        Genre(27, "Horror"),
        Genre(10402, "Music"),
        Genre(9648, "Mystery"),
        Genre(10749, "Romance"),
        Genre(878, "Sci-Fi"),
        Genre(10770, "TV Movie"),
        Genre(53, "Thriller"),
        Genre(10752, "War"),
        Genre(37, "Western")
    )

    private fun fetchFromTmdb(urlString: String, forceType: MediaType? = null): List<MediaItem> {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 6000
            connection.readTimeout = 6000

            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)
                val results = json.optJSONArray("results") ?: return emptyList()
                val list = mutableListOf<MediaItem>()

                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    val id = item.optInt("id")
                    val mediaTypeStr = item.optString("media_type", if (forceType == MediaType.TV) "tv" else "movie")
                    if (mediaTypeStr == "person") continue
                    val isTv = forceType == MediaType.TV || mediaTypeStr == "tv"

                    val title = if (isTv) item.optString("name", item.optString("original_name", "Untitled"))
                    else item.optString("title", item.optString("original_title", "Untitled"))

                    val overview = item.optString("overview", "")
                    val posterPath = item.optString("poster_path", "")
                    val backdropPath = item.optString("backdrop_path", "")
                    val voteAverage = item.optDouble("vote_average", 7.5)
                    val releaseDate = item.optString("release_date", item.optString("first_air_date", "2024-01-01"))

                    val gArray = item.optJSONArray("genre_ids")
                    val genreIdsList = mutableListOf<Int>()
                    if (gArray != null) {
                        for (gIdx in 0 until gArray.length()) {
                            genreIdsList.add(gArray.optInt(gIdx))
                        }
                    }

                    if (id > 0 && title.isNotBlank() && title != "Untitled") {
                        list.add(
                            MediaItem(
                                id = id,
                                tmdbId = id.toString(),
                                title = title,
                                overview = overview,
                                posterPath = if (posterPath.isBlank() || posterPath == "null") null else posterPath,
                                backdropPath = if (backdropPath.isBlank() || backdropPath == "null") null else backdropPath,
                                voteAverage = voteAverage,
                                releaseDate = releaseDate,
                                mediaType = if (isTv) MediaType.TV else MediaType.MOVIE,
                                genreIds = genreIdsList,
                                popularRank = i + 1
                            )
                        )
                    }
                }
                return list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    private fun defaultSeasons(): List<TvSeason> = listOf(
        TvSeason(1, 1, "Season 1", 10, "/9GvhIC31Rm9A39P2338.jpg"),
        TvSeason(2, 2, "Season 2", 8, "/9GvhIC31Rm9A39P2338.jpg"),
        TvSeason(3, 3, "Season 3", 8, "/9GvhIC31Rm9A39P2338.jpg")
    )

    private fun defaultEpisodes(season: Int): List<TvEpisode> = (1..8).map { ep ->
        TvEpisode(
            id = season * 100 + ep,
            episodeNumber = ep,
            seasonNumber = season,
            name = "Episode $ep",
            overview = "An intense chapter unfolding high stakes story events across cinematic landscapes.",
            stillPath = null,
            voteAverage = 8.5
        )
    }

    // High quality rich fallbacks when offline / tmdb api unreachable
    private fun getFallbackTrending(): List<MediaItem> = listOf(
        MediaItem(
            id = 550988,
            tmdbId = "550988",
            title = "Free Guy",
            overview = "A bank teller discovers he is actually a background player in an open-world video game, and decides to become the hero of his own story.",
            posterPath = "/x1031383747.jpg",
            backdropPath = "/y210984712.jpg",
            voteAverage = 7.9,
            releaseDate = "2021-08-11",
            mediaType = MediaType.MOVIE
        ),
        MediaItem(
            id = 299536,
            tmdbId = "299536",
            title = "Avengers: Infinity War",
            overview = "The Avengers and their allies must be willing to sacrifice all in an attempt to defeat the powerful Thanos before his blitz of devastation and ruin puts an end to the universe.",
            posterPath = "/7WsyChLLEz33B32938.jpg",
            backdropPath = "/bOGkgRG3920.jpg",
            voteAverage = 8.3,
            releaseDate = "2018-04-25",
            mediaType = MediaType.MOVIE
        ),
        MediaItem(
            id = 66732,
            tmdbId = "66732",
            title = "Stranger Things",
            overview = "When a young boy vanishes, a small town uncovers a mystery involving secret experiments, terrifying supernatural forces and one strange little girl.",
            posterPath = "/x22119.jpg",
            backdropPath = "/56819283.jpg",
            voteAverage = 8.6,
            releaseDate = "2016-07-15",
            mediaType = MediaType.TV
        ),
        MediaItem(
            id = 157336,
            tmdbId = "157336",
            title = "Interstellar",
            overview = "The adventures of a group of explorers who make use of a newly discovered wormhole to surpass the limitations on human space travel and conquer the vast distances involved in an interstellar voyage.",
            posterPath = "/gEU2A82937.jpg",
            backdropPath = "/rAiYC28.jpg",
            voteAverage = 8.4,
            releaseDate = "2014-11-05",
            mediaType = MediaType.MOVIE
        )
    )

    private fun getFallbackMovies(): List<MediaItem> = listOf(
        MediaItem(
            id = 27205,
            tmdbId = "27205",
            title = "Inception",
            overview = "Cobb, a skilled thief who steals corporate secrets through use of dream-sharing technology, is given the inverse task of planting an idea into the mind of a C.E.O.",
            posterPath = "/oYu232847.jpg",
            backdropPath = "/s3TBr839201.jpg",
            voteAverage = 8.4,
            releaseDate = "2010-07-15",
            mediaType = MediaType.MOVIE
        ),
        MediaItem(
            id = 155,
            tmdbId = "155",
            title = "The Dark Knight",
            overview = "Batman raises the stakes in his war on crime with the help of Lt. Jim Gordon and District Attorney Harvey Dent.",
            posterPath = "/qJ2tW6WMUD.jpg",
            backdropPath = "/nMK2.jpg",
            voteAverage = 8.5,
            releaseDate = "2008-07-16",
            mediaType = MediaType.MOVIE
        ),
        MediaItem(
            id = 603,
            tmdbId = "603",
            title = "The Matrix",
            overview = "Set in the 22nd century, The Matrix tells the story of a computer hacker who joins a group of underground insurgents fighting the 3D world.",
            posterPath = "/f89U320.jpg",
            backdropPath = "/91208.jpg",
            voteAverage = 8.2,
            releaseDate = "1999-03-30",
            mediaType = MediaType.MOVIE
        ),
        MediaItem(
            id = 19995,
            tmdbId = "19995",
            title = "Avatar",
            overview = "In the 22nd century, a paraplegic Marine is dispatched to the moon Pandora on a unique mission, but becomes torn between following orders and protecting an alien civilization.",
            posterPath = "/k12903.jpg",
            backdropPath = "/vL5029.jpg",
            voteAverage = 7.6,
            releaseDate = "2009-12-15",
            mediaType = MediaType.MOVIE
        )
    )

    private fun getFallbackTvShows(): List<MediaItem> = listOf(
        MediaItem(
            id = 1399,
            tmdbId = "1399",
            title = "Game of Thrones",
            overview = "Seven noble families fight for control of the mythical land of Westeros. Friction between the houses leads to full-scale war.",
            posterPath = "/u3bA29.jpg",
            backdropPath = "/2OMB01.jpg",
            voteAverage = 8.4,
            releaseDate = "2011-04-17",
            mediaType = MediaType.TV
        ),
        MediaItem(
            id = 1396,
            tmdbId = "1396",
            title = "Breaking Bad",
            overview = "Walter White, a New Mexico chemistry teacher, is diagnosed with Stage III cancer and decides to enter the methamphetamine making business.",
            posterPath = "/gg2910.jpg",
            backdropPath = "/ts8912.jpg",
            voteAverage = 8.9,
            releaseDate = "2008-01-20",
            mediaType = MediaType.TV
        ),
        MediaItem(
            id = 82856,
            tmdbId = "82856",
            title = "The Mandalorian",
            overview = "After the fall of the Galactic Empire, a lone gunfighter makes his way through the outer reaches of the lawless galaxy.",
            posterPath = "/s39120.jpg",
            backdropPath = "/o92039.jpg",
            voteAverage = 8.5,
            releaseDate = "2019-11-12",
            mediaType = MediaType.TV
        )
    )

    private fun getFallbackTopRated(): List<MediaItem> = listOf(
        MediaItem(
            id = 278,
            tmdbId = "278",
            title = "The Shawshank Redemption",
            overview = "Framed in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison.",
            posterPath = "/q6y0938.jpg",
            backdropPath = "/kXf2910.jpg",
            voteAverage = 8.7,
            releaseDate = "1994-09-23",
            mediaType = MediaType.MOVIE
        ),
        MediaItem(
            id = 238,
            tmdbId = "238",
            title = "The Godfather",
            overview = "Spanning the years 1945 to 1955, a chronicle of the fictional Italian-American Corleone crime family.",
            posterPath = "/3bh3910.jpg",
            backdropPath = "/rSPW290.jpg",
            voteAverage = 8.7,
            releaseDate = "1972-03-14",
            mediaType = MediaType.MOVIE
        )
    )
}
