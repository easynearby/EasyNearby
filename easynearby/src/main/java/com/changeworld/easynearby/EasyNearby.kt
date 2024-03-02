package com.changeworld.easynearby

import com.changeworld.easynearby.advertising.AdvertiseManager
import com.changeworld.easynearby.di.IsolatedKoinContext
import com.changeworld.easynearby.discovery.DiscoveryManager
import com.changeworld.easynearby.exceptions.EasyNearbyNotInitializedException
import com.changeworld.easynearby.loggging.extensions.logD
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Main entry point for easy-nearby
 */
object EasyNearby {

    private val isInitialized = AtomicBoolean(false)
    private val TAG = EasyNearby::class.java.simpleName


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

    @Throws(EasyNearbyNotInitializedException::class)
    fun getAdvertiseManager(): AdvertiseManager {
        if (!isInitialized.get()) {
            throw EasyNearbyNotInitializedException("EasyNearby not initialized")
        }
        return IsolatedKoinContext.koin.get()
    }

    @Throws(EasyNearbyNotInitializedException::class)
    fun getDiscoverManager(): DiscoveryManager {
        if (!isInitialized.get()) {
            throw EasyNearbyNotInitializedException("EasyNearby not initialized")
        }
        return IsolatedKoinContext.koin.get()
    }
}