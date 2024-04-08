package io.github.easynearby.core

import io.github.easynearby.core.advertising.AdvertiseManager
import io.github.easynearby.core.di.IsolatedKoinContext
import io.github.easynearby.core.discovery.DiscoveryManager
import io.github.easynearby.core.exceptions.EasyNearbyNotInitializedException
import io.github.easynearby.core.loggging.extensions.logD
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Main entry point for easy-nearby
 */
object EasyNearby {

    private val isInitialized = AtomicBoolean(false)
    private val TAG = EasyNearby::class.java.simpleName


    /**
     * Should be called by an implementation library that declares [EasyNearbyInitializerFactory]
     */
    @Throws(EasyNearbyNotInitializedException::class)
    fun initialize() {
        try {
            if (isInitialized.getAndSet(true).not()) {
                val factory = FactoryLoader.loadFactory()
                val permissionsChecker = factory.createPermissionsChecker()
                val advertiser = factory.createAdvertiseManager()
                val connector = factory.createConnector()
                val discover = factory.createDiscover()
                val logger = factory.createLogger()

                val dynamicModule = module {
                    single { permissionsChecker }
                    single { advertiser }
                    single { connector }
                    single { discover }
                    single { logger }
                }
                IsolatedKoinContext.koin.loadModules(listOf(dynamicModule))

                logD(TAG, "EasyNearby initialized")

            }
        } catch (e: Throwable) {
            isInitialized.set(false)
            throw EasyNearbyNotInitializedException("Could not initialize easy-nearby", e)
        }
    }

    /**
     * Gets [AdvertiseManager]. If [EasyNearby.initialize] has not been called yet, then throws [EasyNearbyNotInitializedException]
     */
    @Throws(EasyNearbyNotInitializedException::class)
    fun getAdvertiseManager(): AdvertiseManager {
        if (!isInitialized.get()) {
            throw EasyNearbyNotInitializedException("EasyNearby not initialized")
        }
        return IsolatedKoinContext.koin.get()
    }

    /**
     * Gets [DiscoveryManager]. If [EasyNearby.initialize] has not been called yet, then throws [EasyNearbyNotInitializedException]
     */
    @Throws(EasyNearbyNotInitializedException::class)
    fun getDiscoverManager(): DiscoveryManager {
        if (!isInitialized.get()) {
            throw EasyNearbyNotInitializedException("EasyNearby not initialized")
        }
        return IsolatedKoinContext.koin.get()
    }
}