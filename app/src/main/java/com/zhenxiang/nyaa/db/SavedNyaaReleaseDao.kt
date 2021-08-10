package com.zhenxiang.nyaa.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SavedNyaaReleaseDao {

    // Order from most recent
    @Query("SELECT * FROM savednyaarelease ORDER BY timestamp DESC")
    fun getAll(): LiveData<List<SavedNyaaRelease>>

    // Order from most recent
    @Transaction
    @Query("SELECT * From savednyaarelease ORDER BY timestamp DESC")
    fun getAllWithDetails(): LiveData<List<SavedNyaaReleaseWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: SavedNyaaRelease)

    @Query("SELECT * FROM savednyaarelease WHERE releaseId=:id")
    fun getById(id: Int): SavedNyaaRelease?

    @Delete
    fun delete(release: SavedNyaaRelease)
}