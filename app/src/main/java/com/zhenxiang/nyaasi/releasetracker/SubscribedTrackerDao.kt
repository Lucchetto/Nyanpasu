package com.zhenxiang.nyaasi.releasetracker

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SubscribedTrackerDao {


    @Query("SELECT * FROM subscribedtracker WHERE username IS NOT NULL AND searchQuery IS NULL")
    fun getAllTrackedUsers(): List<SubscribedUser>

    @Query("SELECT * FROM subscribedtracker WHERE username IS NOT NULL AND searchQuery IS NULL")
    fun getAllTrackedUsersLive(): LiveData<List<SubscribedTracker>>

    @Query("SELECT * FROM subscribedtracker WHERE searchQuery IS NOT NULL")
    fun getAllTrackedReleases(): List<SubscribedRelease>

    @Query("SELECT * FROM subscribedtracker WHERE searchQuery IS NOT NULL")
    fun getAllTrackedReleasesLive(): LiveData<List<SubscribedTracker>>

    @Query("SELECT * FROM subscribedtracker WHERE searchQuery=:query AND username IS NULL")
    fun getByQueryWithNullUsername(query: String): SubscribedRelease?

    @Query("SELECT * FROM subscribedtracker WHERE username=:username AND searchQuery=:query")
    fun getByUsernameAndQuery(username: String, query: String): SubscribedRelease?

    @Query("SELECT * FROM subscribedtracker WHERE username=:userName AND searchQuery IS NULL")
    fun getByUsername(userName: String): SubscribedUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: SubscribedTracker)

    @Query("UPDATE subscribedtracker SET lastReleaseTimestamp = :timestamp WHERE id = :id")
    fun updateLatestTimestamp(id: Int, timestamp: Long)

    @Query("DELETE FROM subscribedtracker WHERE username=:userName AND searchQuery IS NULL")
    fun deleteByUsername(userName: String)
}
