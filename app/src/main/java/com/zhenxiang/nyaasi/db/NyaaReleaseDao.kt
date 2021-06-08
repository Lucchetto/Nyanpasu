package com.zhenxiang.nyaasi.db

import androidx.room.*

@Dao
interface NyaaReleaseDao {

    @Query("SELECT * FROM nyaarelease")
    fun getAll(): List<NyaaRelease>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: NyaaRelease)

    @Delete
    fun delete(release: NyaaRelease)

    @Update
    fun update(release: NyaaRelease)
}