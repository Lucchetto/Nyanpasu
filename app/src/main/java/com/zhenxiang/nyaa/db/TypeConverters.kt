package com.zhenxiang.nyaa.db

import androidx.room.TypeConverter
import com.zhenxiang.nyaa.api.ApiDataSource
import com.zhenxiang.nyaa.api.NyaaReleaseCategory
import com.zhenxiang.nyaa.api.ReleaseCategory

class DbTypeConverters {

    companion object {
        @TypeConverter @JvmStatic
        // Use ~ for separating data source from actual category
        fun fromReleaseCategoryToColumn(category: ReleaseCategory?): String? {
            return category?.let {
                return "${it.getDataSource().value}~${it.getId()}"
            } ?: run {
                null
            }
        }

        @TypeConverter @JvmStatic
        fun fromColumnToReleaseCategory(column: String?): ReleaseCategory? {
            if (column == null) {
                return null
            }
            // Since we store category in the format "dataSource~id"
            // We have to parse the first splitted part as Int
            val columnSplit = column.split("~")
            return if (columnSplit.size == 2) {
                val dataSourceValue: ApiDataSource
                try {
                    dataSourceValue = ApiDataSource.valueOf(columnSplit[0].toInt())
                } catch(e: Exception) {
                    return null
                }
                // Then we check if data dataSource is valid and get the array of the correct enum
                val categoriesArray = when (dataSourceValue) {
                    ApiDataSource.NYAA_SI -> NyaaReleaseCategory.values()
                }
                // Find the right enum value for the saved id and return
                categoriesArray.forEach {
                    if (it.getId() == columnSplit[1]) {
                        return it
                    }
                }

                // Return null if nothing found
                null
            } else {
                // Return null if column string is invalid
                null
            }
        }

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