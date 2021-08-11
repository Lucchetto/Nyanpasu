package com.zhenxiang.nyaa.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zhenxiang.nyaa.releasetracker.SubscribedTracker
import com.zhenxiang.nyaa.releasetracker.SubscribedTrackerDao

@Database(entities = [NyaaReleasePreview::class, NyaaReleaseDetails::class, NyaaSearchHistoryItem::class,
    ViewedNyaaRelease::class, SavedNyaaRelease::class, SubscribedTracker::class], version = 2,
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

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE nyaareleasepreview RENAME COLUMN id TO number;")
            }
        }


        @Volatile private var instance: NyaaDb? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            NyaaDb::class.java, "local_nyaa.db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }
}
