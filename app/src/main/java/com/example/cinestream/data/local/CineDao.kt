package com.example.cinestream.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CineDao {

    // Watchlist
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE tmdbId = :tmdbId LIMIT 1)")
    fun isInWatchlist(tmdbId: String): Flow<Boolean>

    @Query("SELECT * FROM watchlist WHERE tmdbId = :tmdbId LIMIT 1")
    suspend fun getWatchlistItemById(tmdbId: String): WatchlistEntity?

    @Query("SELECT COUNT(*) FROM watchlist")
    fun getWatchlistCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE tmdbId = :tmdbId")
    suspend fun deleteFromWatchlist(tmdbId: String)

    @Query("DELETE FROM watchlist")
    suspend fun clearWatchlist()

    // Watch History
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHistory(entity: HistoryEntity)

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun deleteHistoryItem(id: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()

    // Offline Downloads
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDownload(entity: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: String)
}
