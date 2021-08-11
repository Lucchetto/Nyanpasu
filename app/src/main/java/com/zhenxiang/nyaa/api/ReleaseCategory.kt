package com.zhenxiang.nyaa.api

interface ReleaseCategory {
    fun getDataSource(): ApiDataSource
    fun getId(): String
    fun getStringResId(): Int
}
