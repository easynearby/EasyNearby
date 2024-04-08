package io.github.easynearby.core.loggging.extensions

import io.github.easynearby.core.di.IsolatedKoinContext
import io.github.easynearby.core.loggging.Logger


private val logger = IsolatedKoinContext.koin.get<Logger>()

internal fun logD(tag: String, msg: String, throwable: Throwable? = null) {
    logger.logD(tag, msg, throwable)
}

internal fun logI(tag: String, msg: String, throwable: Throwable? = null) {
    logger.logI(tag, msg, throwable)
}

internal fun logW(tag: String, msg: String, throwable: Throwable? = null) {
    logger.logW(tag, msg, throwable)
}

internal fun logE(tag: String, msg: String, throwable: Throwable? = null) {
    logger.logE(tag, msg, throwable)
}