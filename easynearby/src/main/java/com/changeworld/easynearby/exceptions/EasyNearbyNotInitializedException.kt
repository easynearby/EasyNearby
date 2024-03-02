package com.changeworld.easynearby.exceptions

class EasyNearbyNotInitializedException(
    override val message: String?,
    override val cause: Throwable? = null
) : RuntimeException()