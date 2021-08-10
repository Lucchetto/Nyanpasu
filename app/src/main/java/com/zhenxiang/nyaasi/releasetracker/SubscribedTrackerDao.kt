package com.zhenxiang.nyaasi.releasetracker

import androidx.lifecycle.LiveData
import androidx.room.*
import com.zhenxiang.nyaasi.api.NyaaReleaseCategory

@Dao
interface SubscribedTrackerDao {

    @Query("SELECT * FROM subscribedtracker")
    fun getAllTrackers(): List<SubscribedTracker>

    @Query("SELECT * FROM subscribedtracker")
    fun getAllTrackersLive(): LiveData<List<SubscribedTracker>>

    @Query("SELECT * FROM subscribedtracker WHERE username=:username AND searchQuery=:query AND category=:category")
    fun getByUsernameAndQueryAndCategory(username: String?, query: String, category: NyaaReleaseCategory): SubscribedTracker?

    @Query("SELECT * FROM subscribedtracker WHERE username=:userName AND searchQuery IS NULL")
    fun getByUsername(userName: String): SubscribedTracker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tracker: SubscribedTracker)

    @Update
    fun update(tracker: SubscribedTracker)

    @Query(" UPDATE subscribedtracker SET newReleasesCount=0 WHERE id=:id")
    fun clearNewReleasesCount(id: Int)

    @Query("DELETE FROM subscribedtracker WHERE username=:userName AND searchQuery IS NULL")
    fun deleteByUsername(userName: String)

    @Query("DELETE FROM subscribedtracker WHERE id=:id")
    fun deleteById(id: Int)
}
