package com.zhenxiang.nyaa.db

import androidx.room.TypeConverter
import com.zhenxiang.nyaa.api.ApiDataSource
import com.zhenxiang.nyaa.api.NyaaReleaseCategory
import com.zhenxiang.nyaa.api.ReleaseCategory

class DbTypeConverters {

    companion object {
        @TypeConverter @JvmStatic
        fun fromDataSourceToInt(dataSource: ApiDataSource): Int {
            return dataSource.value
        }

        @TypeConverter @JvmStatic
        fun fromIntToDataSource(source: Int): ApiDataSource {
            return ApiDataSource.valueOf(source)
        }
    }
}