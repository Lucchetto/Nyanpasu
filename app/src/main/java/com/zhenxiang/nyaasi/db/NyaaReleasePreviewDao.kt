package com.zhenxiang.nyaasi.db

import androidx.room.*
import android.provider.SyncStateContract.Helpers.update

import android.provider.SyncStateContract.Helpers.insert




@Dao
interface NyaaReleasePreviewDao {

    @Query("SELECT * FROM nyaareleasepreview")
    fun getAll(): List<NyaaReleasePreview>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(release: NyaaReleasePreview): Long

    @Query("DELETE FROM nyaareleasepreview WHERE id = :id")
    fun deleteById(id: Int)

    @Update
    fun update(release: NyaaReleasePreview)

    @Transaction
    fun upsert(release: NyaaReleasePreview) {
        val id: Long = insert(release)
        if (id == -1L) {
            update(release)
        }
    }

    @Query("delete from nyaareleasepreview where id in (:list)")
    fun deleteByIdList(list: List<Int>)
}

@Dao
interface NyaaReleaseDetailsDao {

    @Query("SELECT * FROM nyaareleasedetails")
    fun getAll(): List<NyaaReleaseDetails>

    @Query("SELECT * FROM nyaareleasedetails WHERE parentId=:id")
    fun getById(id: Int): NyaaReleaseDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: NyaaReleaseDetails)

    @Delete
    fun delete(release: NyaaReleaseDetails)

    @Update
    fun update(release: NyaaReleaseDetails)
}
