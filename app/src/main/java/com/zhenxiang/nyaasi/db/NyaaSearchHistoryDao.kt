package com.zhenxiang.nyaasi.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NyaaSearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: NyaaSearchHistoryItem)

    @Query("SELECT * FROM nyaasearchhistoryitem ORDER BY searchTimestamp DESC")
    fun getAll(): LiveData<List<NyaaSearchHistoryItem>>
}