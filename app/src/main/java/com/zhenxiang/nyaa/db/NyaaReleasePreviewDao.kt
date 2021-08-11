package com.zhenxiang.nyaa.db

import androidx.room.*
import com.zhenxiang.nyaa.api.ApiDataSource

@Dao
interface NyaaReleasePreviewDao {

    @Query("SELECT * FROM nyaareleasepreview")
    fun getAll(): List<NyaaReleasePreview>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(release: NyaaReleasePreview): Long

    @Query("DELETE FROM nyaareleasepreview WHERE number = :number AND dataSource = :dataSource")
    fun delete(number: Int, dataSource: ApiDataSource)

    @Update
    fun update(release: NyaaReleasePreview)

    @Transaction
    fun upsert(release: NyaaReleasePreview) {
        val id: Long = insert(release)
        if (id == -1L) {
            update(release)
        }
    }

    @Query("delete from nyaareleasepreview WHERE number=:number AND dataSource=:dataSource")
    fun deleteById(number: Int, dataSource: ApiDataSource)

    @Transaction
    fun deleteByIdList(list: List<ReleaseId>) {
        list.forEach {
            deleteById(it.number, it.dataSource)
        }
    }
}

@Dao
interface NyaaReleaseDetailsDao {

    @Query("SELECT * FROM nyaareleasedetails")
    fun getAll(): List<NyaaReleaseDetails>

    @Query("SELECT * FROM nyaareleasedetails WHERE parent_number=:number AND parent_dataSource=:dataSource")
    fun getItemById(number: Int, dataSource: ApiDataSource): NyaaReleaseDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: NyaaReleaseDetails)

    @Delete
    fun delete(release: NyaaReleaseDetails)

    @Update
    fun update(release: NyaaReleaseDetails)
}
