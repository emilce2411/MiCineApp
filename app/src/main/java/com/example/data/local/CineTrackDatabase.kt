package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CustomList
import com.example.data.model.CustomListItemCrossRef
import com.example.data.model.Episode
import com.example.data.model.MediaItem
import com.example.data.model.UserMediaState
import com.example.data.model.WatchedEpisodeHistory

@Database(
    entities = [
        MediaItem::class,
        UserMediaState::class,
        Episode::class,
        CustomList::class,
        CustomListItemCrossRef::class,
        WatchedEpisodeHistory::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CineTrackDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun userMediaDao(): UserMediaDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun customListDao(): CustomListDao
    abstract fun watchedEpisodeHistoryDao(): WatchedEpisodeHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: CineTrackDatabase? = null

        fun getDatabase(context: Context): CineTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CineTrackDatabase::class.java,
                    "cinetrack_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
