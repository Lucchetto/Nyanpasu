package com.zhenxiang.nyaa.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SavedNyaaReleaseDao {

    // Order from most recent
    @Query("SELECT * FROM savednyaarelease ORDER BY savedTimestamp DESC")
    fun getAll(): LiveData<List<SavedNyaaRelease>>

    // Order from most recent
    @Query("SELECT * From savednyaarelease INNER JOIN nyaareleasepreview ON id=releaseId AND dataSource=releaseDataSource ORDER BY savedTimestamp DESC")
    fun getAllWithDetails(): LiveData<List<SavedNyaaReleaseWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: SavedNyaaRelease)

    @Query("SELECT * FROM savednyaarelease WHERE releaseId=:id")
    fun getById(id: Int): SavedNyaaRelease?

    @Delete
    fun delete(release: SavedNyaaRelease)
}