package com.zhenxiang.nyaa.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NyaaSearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: NyaaSearchHistoryItem)

    @Query("SELECT * FROM nyaasearchhistoryitem ORDER BY searchTimestamp DESC")
    fun getAllLive(): LiveData<List<NyaaSearchHistoryItem>>

    @Query("DELETE FROM nyaasearchhistoryitem WHERE searchQuery NOT IN (SELECT searchQuery FROM nyaasearchhistoryitem ORDER BY searchTimestamp DESC LIMIT 25)")
    fun deleteExcessiveRecents(): Int

    @Delete
    fun delete(item: NyaaSearchHistoryItem)
}