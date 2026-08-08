package com.example.cinestream.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val tmdbId: String,
    val mediaType: String, // "movie" or "tv"
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    val releaseYear: String,
    val overview: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class HistoryEntity(
    @PrimaryKey val id: String, // "$tmdbId-$mediaType-$season-$episode"
    val tmdbId: String,
    val mediaType: String,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val season: Int = 1,
    val episode: Int = 1,
    val progressSeconds: Long = 0,
    val totalDurationSeconds: Long = 0,
    val lastWatchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String, // "$tmdbId-$mediaType-$season-$episode"
    val tmdbId: String,
    val mediaType: String,
    val title: String,
    val posterPath: String?,
    val season: Int = 1,
    val episode: Int = 1,
    val progressMb: Double = 0.0,
    val totalMb: Double = 1250.0,
    val status: String = "DOWNLOADING", // "DOWNLOADING", "PAUSED", "COMPLETED"
    val downloadedAt: Long = System.currentTimeMillis()
)
