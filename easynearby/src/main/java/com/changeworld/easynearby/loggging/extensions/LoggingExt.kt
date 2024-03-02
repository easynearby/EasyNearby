package com.changeworld.easynearby.loggging.extensions

import com.changeworld.easynearby.di.IsolatedKoinContext
import com.changeworld.easynearby.loggging.Logger


private val logger = IsolatedKoinContext.koin.get<Logger>()

internal fun Any.logD(tag: String, msg: String, throwable: Throwable? = null) {
    logger.logD(tag, msg, throwable)
}

internal fun Any.logI(tag: String, msg: String, throwable: Throwable? = null) {
    logger.logI(tag, msg, throwable)
}

internal fun Any.logW(tag: String, msg: String, throwable: Throwable? = null) {
    logger.logW(tag, msg, throwable)
}

internal fun Any.logE(tag: String, msg: String, throwable: Throwable? = null) {
    logger.logE(tag, msg, throwable)
}