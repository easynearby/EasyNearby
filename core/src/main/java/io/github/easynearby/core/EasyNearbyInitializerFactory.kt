package io.github.easynearby.core

import io.github.easynearby.core.advertising.Advertiser
import io.github.easynearby.core.connection.Connector
import io.github.easynearby.core.discovery.Discover
import io.github.easynearby.core.loggging.Logger

/**
 * Interface that should be implemented by an implementation library. An implementation should follow
 * instructions in order to initialize easy-nearby [ServiceLoader](https://docs.oracle.com/javase%2F7%2Fdocs%2Fapi%2F%2F/java/util/ServiceLoader.html)
 */
interface EasyNearbyInitializerFactory {
    fun createPermissionsChecker(): PermissionsChecker
    fun createAdvertiseManager(): Advertiser
    fun createConnector(): Connector
    fun createDiscover(): Discover
    fun createLogger(): Logger
}