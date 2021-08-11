package com.zhenxiang.nyaa.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ViewedNyaaReleaseDao {

    // Order from most recent
    @Query("SELECT * FROM viewednyaarelease ORDER BY viewedTimestamp DESC")
    fun getAll(): LiveData<List<ViewedNyaaRelease>>

    // Order from most recent
    @Query("SELECT * FROM viewednyaarelease INNER JOIN nyaareleasepreview ON number=parent_number AND dataSource=parent_dataSource ORDER BY viewedTimestamp DESC")
    fun getAllWithDetails(): LiveData<List<ViewedNyaaReleaseWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: ViewedNyaaRelease)

    // Let's limit the list to the most 150 recent items
    @Query("""
            WITH to_keep AS (SELECT parent_number, parent_dataSource FROM viewednyaarelease ORDER BY viewedTimestamp DESC LIMIT 150)
            SELECT parent_number AS number, parent_dataSource AS dataSource FROM viewednyaarelease WHERE NOT EXISTS (SELECT parent_number, parent_dataSource FROM to_keep
                WHERE to_keep.parent_number=viewednyaarelease.parent_number AND to_keep.parent_dataSource=viewednyaarelease.parent_dataSource)
    """)
    fun getExcessiveRecentsIds(): List<ReleaseId>

    @Delete
    fun delete(relase: ViewedNyaaRelease)

    @Query("delete from viewednyaarelease WHERE parent_number=:number AND parent_dataSource=:dataSource")
    fun deleteById(number: Int, dataSource: Int)

    @Transaction
    fun deleteByIdList(list: List<ReleaseId>) {
        list.forEach {
            deleteById(it.number, it.dataSource)
        }
    }
}