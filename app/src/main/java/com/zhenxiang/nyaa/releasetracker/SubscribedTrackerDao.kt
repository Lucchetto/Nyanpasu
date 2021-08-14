package com.zhenxiang.nyaa.releasetracker

import androidx.lifecycle.LiveData
import androidx.room.*
import com.zhenxiang.nyaa.api.ApiDataSource
import com.zhenxiang.nyaa.api.NyaaReleaseCategory
import com.zhenxiang.nyaa.db.DbTypeConverters

@Dao
interface SubscribedTrackerDao {

    @Query("SELECT * FROM subscribedtracker ORDER BY latestReleaseTimestamp DESC")
    fun getAllTrackers(): List<SubscribedTracker>

    @Query("SELECT * FROM subscribedtracker ORDER BY latestReleaseTimestamp DESC")
    fun getAllTrackersLive(): LiveData<List<SubscribedTracker>>

    @Query("SELECT * FROM subscribedtracker WHERE (username=:username OR (username is null and :username is null)) AND searchQuery=:query AND categoryId=:categoryId AND dataSource=:dataSource")
    fun getBySpecs(username: String?, query: String, categoryId: String, dataSource: ApiDataSource): SubscribedTracker?

    @Query("SELECT * FROM subscribedtracker WHERE username=:username AND searchQuery IS NULL AND dataSource=:dataSource")
    fun getByUsername(username: String, dataSource: ApiDataSource): SubscribedTracker?

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
