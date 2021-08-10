package com.zhenxiang.nyaa.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NyaaSearchHistoryItem(@PrimaryKey @ColumnInfo(collate = ColumnInfo.NOCASE) val searchQuery: String,
                                 var searchTimestamp: Long,
)