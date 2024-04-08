package io.github.easynearby.core

import io.github.easynearby.core.advertising.Advertiser
import io.github.easynearby.core.connection.Connector
import io.github.easynearby.core.discovery.Discover
import io.github.easynearby.core.loggging.Logger

/**
 * Interface that should be implemented by an implementation library
 */
interface EasyNearbyInitializerFactory {
    fun createPermissionsChecker(): PermissionsChecker
    fun createAdvertiseManager(): Advertiser
    fun createConnector(): Connector
    fun createDiscover(): Discover
    fun createLogger(): Logger
}