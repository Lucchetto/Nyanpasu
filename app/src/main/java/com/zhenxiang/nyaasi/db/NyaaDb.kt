package com.zhenxiang.nyaasi.db

import android.content.Context
import androidx.room.*
import com.zhenxiang.nyaasi.releasetracker.SubscribedTracker
import com.zhenxiang.nyaasi.releasetracker.SubscribedTrackerDao

@Database(entities = [NyaaReleasePreview::class, NyaaReleaseDetails::class, NyaaSearchHistoryItem::class,
    ViewedNyaaRelease::class, SavedNyaaRelease::class, SubscribedTracker::class], version = 1,
)
@TypeConverters(DbTypeConverters::class)
abstract class NyaaDb : RoomDatabase() {
    abstract fun nyaaReleasesPreviewDao(): NyaaReleasePreviewDao
    abstract fun nyaaReleasesDetailsDao(): NyaaReleaseDetailsDao
    abstract fun nyaaSearchHistoryDao(): NyaaSearchHistoryDao
    abstract fun viewedNyaaReleasesDao(): ViewedNyaaReleaseDao
    abstract fun savedNyaaReleasesDao(): SavedNyaaReleaseDao

    abstract fun subscribedTrackersDao(): SubscribedTrackerDao

    companion object {

        @Volatile private var instance: NyaaDb? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            NyaaDb::class.java, "local_nyaa.db")
            .build()
    }
}
