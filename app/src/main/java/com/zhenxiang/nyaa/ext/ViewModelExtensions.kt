package com.zhenxiang.nyaa.ext

import androidx.lifecycle.SavedStateHandle

fun<T> SavedStateHandle.getNonNull(key: String, defaultValue: T) = get<T>(key) ?: defaultValue