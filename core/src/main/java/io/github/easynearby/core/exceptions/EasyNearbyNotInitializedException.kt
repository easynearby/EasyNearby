package io.github.easynearby.core.exceptions

class EasyNearbyNotInitializedException(
    override val message: String?,
    override val cause: Throwable? = null
) : RuntimeException()