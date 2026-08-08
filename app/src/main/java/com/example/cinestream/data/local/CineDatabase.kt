package com.example.cinestream.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WatchlistEntity::class, HistoryEntity::class, DownloadEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CineDatabase : RoomDatabase() {

    abstract fun cineDao(): CineDao

    companion object {
        @Volatile
        private var INSTANCE: CineDatabase? = null

        fun getDatabase(context: Context): CineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CineDatabase::class.java,
                    "cinestream_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
