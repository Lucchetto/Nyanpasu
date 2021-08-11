package com.zhenxiang.nyaa.db

import androidx.room.TypeConverter
import com.zhenxiang.nyaa.api.ApiDataSource
import com.zhenxiang.nyaa.api.NyaaReleaseCategory

class DbTypeConverters {

    @TypeConverter
    fun fromNyaaReleaseCategoryToId(category: NyaaReleaseCategory): String {
        return category.id
    }

    @TypeConverter
    fun fromIdToNyaaReleaseCategory(id: String): NyaaReleaseCategory {
        return try {
            NyaaReleaseCategory.valueOf(id)
        } catch (e: IllegalArgumentException) {
            NyaaReleaseCategory.ALL
        }
    }

    @TypeConverter
    fun fromDataSourceToInt(dataSource: ApiDataSource): Int {
        return dataSource.value
    }

    @TypeConverter
    fun fromIntToDataSource(source: Int): ApiDataSource {
        return ApiDataSource.valueOf(source)
    }
}