package com.zhenxiang.nyaasi.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ViewedNyaaReleaseDao {

    // Order from most recent
    @Query("SELECT * FROM viewednyaarelease ORDER BY timestamp DESC")
    fun getAll(): LiveData<List<ViewedNyaaRelease>>

    // Order from most recent
    @Transaction
    @Query("SELECT * From viewednyaarelease ORDER BY timestamp DESC")
    fun getAllWithDetails(): LiveData<List<ViewedNyaaReleaseWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: ViewedNyaaRelease)

    @Delete
    fun delete(release: ViewedNyaaRelease)
}