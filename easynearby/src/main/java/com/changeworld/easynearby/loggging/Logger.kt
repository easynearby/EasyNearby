package com.changeworld.easynearby.loggging

interface Logger {
    fun logD(tag: String, msg: String, throwable: Throwable?)
    fun logI(tag: String, msg: String, throwable: Throwable?)
    fun logE(tag: String, msg: String, throwable: Throwable?)
    fun logW(tag: String, msg: String, throwable: Throwable?)
}