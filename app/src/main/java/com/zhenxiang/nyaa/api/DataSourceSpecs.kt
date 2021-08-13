package com.zhenxiang.nyaa.api

import androidx.room.ColumnInfo
import androidx.room.Ignore
import java.io.Serializable

class DataSourceSpecs: Serializable {
    @ColumnInfo(name = "dataSource") val source: ApiDataSource
    val categoryId: String
    @Ignore val category: ReleaseCategory

    constructor(source: ApiDataSource, categoryId: String) {
        this.source = source
        this.categoryId = categoryId
        this.category = getCategoryFromId(categoryId)
    }

    constructor(dataSource: ApiDataSource, category: ReleaseCategory) {
        this.source = dataSource
        if (category.getDataSource() == dataSource) {
            this.categoryId = category.getId()
            this.category = category
        } else {
            this.categoryId = this.source.categories[0].getId()
            this.category = this.source.categories[0]
        }
    }

    private fun getCategoryFromId(categoryId: String): ReleaseCategory {
        for (category in source.categories) {
            if (categoryId == category.getId()) {
                return category
            }
        }
        return source.categories[0]
    }
}