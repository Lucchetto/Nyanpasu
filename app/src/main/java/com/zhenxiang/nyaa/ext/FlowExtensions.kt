package com.zhenxiang.nyaa.ext

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

val <T> SharedFlow<T>.latestValue: T?
        get() = replayCache.lastOrNull()

inline fun <T> Flow<T>.collectInScope(
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        crossinline action: suspend (value: T) -> Unit,
) = coroutineScope.launch(dispatcher) {
        collect { action(it) }
}

inline fun <T> Flow<T>.collectInLifecycle(
        lifecycleOwner: LifecycleOwner,
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        crossinline action: suspend (value: T) -> Unit,
) = collectInScope(lifecycleOwner.lifecycleScope, dispatcher, action)
