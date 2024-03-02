package com.changeworld.easynearby

import com.changeworld.easynearby.advertising.Advertiser
import com.changeworld.easynearby.connection.Connector
import com.changeworld.easynearby.discovery.Discover
import com.changeworld.easynearby.loggging.Logger

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