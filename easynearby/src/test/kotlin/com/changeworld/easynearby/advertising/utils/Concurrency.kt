package com.changeworld.easynearby.advertising.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import java.util.concurrent.Executors

internal fun <T> CoroutineScope.setUpConcurrentCalls(
    numberOfTreads: Int, call: suspend () -> T
): List<Deferred<T>> {
    val coroutinesDispatcher = Executors.newFixedThreadPool(numberOfTreads).asCoroutineDispatcher()
    return buildList {
        repeat(numberOfTreads) {
            add(
                async(context = coroutinesDispatcher, start = CoroutineStart.LAZY) {
                    call()
                }
            )
        }
    }
}