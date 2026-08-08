package com.example.cinestream.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MediaType {
    MOVIE, TV
}

data class MediaItem(
    val id: Int,
    val tmdbId: String,
    val title: String,
    val name: String? = null,
    val overview: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val voteAverage: Double = 0.0,
    val releaseDate: String? = null,
    val firstAirDate: String? = null,
    val mediaType: MediaType = MediaType.MOVIE,
    val genreIds: List<Int> = emptyList(),
    val popularRank: Int = 0
) {
    val displayTitle: String
        get() = title.ifBlank { name ?: "Untitled" }

    val releaseYear: String
        get() = (releaseDate ?: firstAirDate ?: "").take(4)

    val fullPosterUrl: String
        get() = if (!posterPath.isNullOrBlank()) {
            if (posterPath.startsWith("http")) posterPath
            else "https://image.tmdb.org/t/p/w500$posterPath"
        } else "https://picsum.photos/300/450"

    val fullBackdropUrl: String
        get() = if (!backdropPath.isNullOrBlank()) {
            if (backdropPath.startsWith("http")) backdropPath
            else "https://image.tmdb.org/t/p/w1280$backdropPath"
        } else "https://picsum.photos/1280/720"
}

data class TvSeason(
    val id: Int,
    val seasonNumber: Int,
    val name: String,
    val episodeCount: Int,
    val posterPath: String? = null
)

data class TvEpisode(
    val id: Int,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val name: String,
    val overview: String = "",
    val stillPath: String? = null,
    val voteAverage: Double = 0.0
)

data class Genre(
    val id: Int,
    val name: String
)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profilePath: String? = null
)
