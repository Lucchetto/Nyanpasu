package com.zhenxiang.nyaasi.releasetracker

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SubscribedTrackerDao {

    @Query("SELECT * FROM subscribedtracker")
    fun getAllTrackers(): List<SubscribedTracker>

    @Query("SELECT * FROM subscribedtracker")
    fun getAllTrackersLive(): LiveData<List<SubscribedTracker>>

    @Query("SELECT * FROM subscribedtracker WHERE searchQuery=:query AND username IS NULL")
    fun getByQueryWithNullUsername(query: String): SubscribedTracker?

    @Query("SELECT * FROM subscribedtracker WHERE username=:username AND searchQuery=:query")
    fun getByUsernameAndQuery(username: String, query: String): SubscribedTracker?

    @Query("SELECT * FROM subscribedtracker WHERE username=:userName AND searchQuery IS NULL")
    fun getByUsername(userName: String): SubscribedTracker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tracker: SubscribedTracker)

    @Update
    fun update(tracker: SubscribedTracker)

    @Query("DELETE FROM subscribedtracker WHERE username=:userName AND searchQuery IS NULL")
    fun deleteByUsername(userName: String)
}
