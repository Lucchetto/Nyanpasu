package com.zhenxiang.nyaasi.db

import androidx.room.*

@Dao
interface ViewedNyaaReleaseDao {

    @Query("SELECT * FROM viewednyaarelease")
    fun getAll(): List<ViewedNyaaRelease>

    @Transaction
    @Query("SELECT * From nyaarelease")
    fun getAllWithDetails(): List<ViewedNyaaReleaseWithDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: ViewedNyaaRelease)

    @Delete
    fun delete(release: ViewedNyaaRelease)
}