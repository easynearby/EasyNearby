package com.changeworld.easynearby

import java.util.ServiceLoader

/**
 * Class that helps to load [EasyNearbyInitializerFactory]. Uses [ServiceLoader] under the hood.
 * Mainly was introduced to make easy-nearby easier to test (could not mock [ServiceLoader])
 */
object FactoryLoader {
    fun loadFactory(): EasyNearbyInitializerFactory {
        return ServiceLoader.load(EasyNearbyInitializerFactory::class.java).iterator().next()
    }
}