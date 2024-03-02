package com.changeworld.easynearby.advertising.testimpl

import com.changeworld.easynearby.loggging.Logger

class ConsoleLoggerImpl : Logger {
    override fun logD(tag: String, msg: String, throwable: Throwable?) {
        println("$tag: $msg")
    }

    override fun logI(tag: String, msg: String, throwable: Throwable?) {
        println("$tag: $msg")
    }

    override fun logE(tag: String, msg: String, throwable: Throwable?) {
        println("$tag: $msg")
    }

    override fun logW(tag: String, msg: String, throwable: Throwable?) {
        println("$tag: $msg")
    }
}