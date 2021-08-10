package com.zhenxiang.nyaa.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ViewedNyaaReleaseDao {

    // Order from most recent
    @Query("SELECT * FROM viewednyaarelease ORDER BY viewedTimestamp DESC")
    fun getAll(): LiveData<List<ViewedNyaaRelease>>

    // Order from most recent
    @Query("SELECT * FROM viewednyaarelease INNER JOIN nyaareleasepreview ON id=releaseId AND dataSource=releaseDataSource ORDER BY viewedTimestamp DESC")
    fun getAllWithDetails(): LiveData<List<ViewedNyaaReleaseWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: ViewedNyaaRelease)

    // Let's limit the list to the most 150 recent items
    @Query("SELECT releaseId FROM viewednyaarelease WHERE releaseId NOT IN (SELECT releaseId FROM viewednyaarelease ORDER BY viewedTimestamp DESC LIMIT 150)")
    fun getExcessiveRecentsIds(): List<Int>

    @Delete
    fun delete(release: ViewedNyaaRelease)

    @Query("delete from viewednyaarelease where releaseId=:id")
    fun deleteById(id: Int)

    @Query("delete from viewednyaarelease where releaseId in (:list)")
    fun deleteByIdList(list: List<Int>)
}