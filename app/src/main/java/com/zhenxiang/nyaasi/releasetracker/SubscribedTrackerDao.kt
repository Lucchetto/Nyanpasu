package com.zhenxiang.nyaasi.releasetracker

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SubscribedTrackerDao {

    @Query("SELECT * FROM subscribedtracker")
    fun getAllLive(): LiveData<List<SubscribedTracker>>

    @Query("SELECT * FROM subscribedtracker WHERE username IS NOT NULL")
    fun getAllTrackedUsers(): List<SubscribedUser>

    @Query("SELECT * FROM subscribedtracker WHERE searchQuery IS NOT NULL")
    fun getAllTrackedReleases(): List<SubscribedRelease>

    @Query("SELECT * FROM subscribedtracker WHERE username=:userName")
    fun getByUsername(userName: String): SubscribedUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: SubscribedTracker)

    @Query("UPDATE subscribedtracker SET lastReleaseTimestamp = :timestamp WHERE id = :id")
    fun updateLatestTimestamp(id: Int, timestamp: Long)

    @Query("DELETE FROM subscribedtracker WHERE username=:userName")
    fun deleteByUsername(userName: String)
}
