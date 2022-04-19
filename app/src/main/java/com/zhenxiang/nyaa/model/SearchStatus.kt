package com.zhenxiang.nyaa.model

interface SearchStatus {

    object Ready: SearchStatus

    object Loading: SearchStatus

    object End: SearchStatus

    object Error: SearchStatus
}