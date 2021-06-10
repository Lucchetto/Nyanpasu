package com.zhenxiang.nyaasi.db

import androidx.room.*

@Dao
interface NyaaReleasePreviewDao {

    @Query("SELECT * FROM nyaareleasepreview")
    fun getAll(): List<NyaaReleasePreview>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(release: NyaaReleasePreview)

    @Delete
    fun delete(release: NyaaReleasePreview)

    @Update
    fun update(release: NyaaReleasePreview)
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
