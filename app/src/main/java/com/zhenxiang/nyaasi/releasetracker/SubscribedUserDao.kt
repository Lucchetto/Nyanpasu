package com.zhenxiang.nyaasi.releasetracker

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SubscribedUserDao {

    @Query("SELECT * FROM subscribeduser")
    fun getAllLive(): LiveData<List<SubscribedUser>>

    @Query("SELECT * FROM subscribeduser")
    fun getAll(): List<SubscribedUser>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: SubscribedUser)

    @Delete
    fun delete(release: SubscribedUser)
}
