package com.zhenxiang.nyaasi.releasetracker

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NewReleaseDao {
    @Insert
    fun insertAll(vararg newReleases: NewRelease)

    @Delete
    fun delete(release: NewRelease)

    @Query("SELECT * FROM newrelease")
    fun getAll(): List<NewRelease>
}
