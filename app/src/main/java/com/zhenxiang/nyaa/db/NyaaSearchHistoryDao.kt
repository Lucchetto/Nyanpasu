package com.zhenxiang.nyaa.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NyaaSearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: NyaaSearchHistoryItem)

    @Query("SELECT * FROM nyaasearchhistoryitem ORDER BY searchTimestamp DESC")
    fun getAllFlow(): Flow<List<NyaaSearchHistoryItem>>

    @Query("DELETE FROM nyaasearchhistoryitem WHERE searchQuery NOT IN (SELECT searchQuery FROM nyaasearchhistoryitem ORDER BY searchTimestamp DESC LIMIT 25)")
    fun deleteExcessiveRecents(): Int

    @Delete
    fun delete(item: NyaaSearchHistoryItem)
}