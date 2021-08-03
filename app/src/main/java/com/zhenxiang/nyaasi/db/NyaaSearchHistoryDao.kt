package com.zhenxiang.nyaasi.db

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NyaaSearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: NyaaSearchHistoryItem)

    @Query("SELECT * FROM nyaasearchhistoryitem ORDER BY searchTimestamp DESC")
    fun getAllLive(): LiveData<List<NyaaSearchHistoryItem>>

    @Query("SELECT *, 0 AS _id FROM nyaasearchhistoryitem ORDER BY searchTimestamp DESC")
    fun getAllAsCursor(): Cursor

    @Query("SELECT *, 0 AS _id FROM nyaasearchhistoryitem WHERE LOWER(searchQuery) LIKE LOWER(:query) ORDER BY searchTimestamp DESC")
    fun searchByQueryAsCursor(query: String): Cursor
}