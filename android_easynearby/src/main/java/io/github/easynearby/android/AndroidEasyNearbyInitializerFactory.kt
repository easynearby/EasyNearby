package io.github.easynearby.android

import io.github.easynearby.core.EasyNearbyInitializerFactory
import io.github.easynearby.core.PermissionsChecker
import io.github.easynearby.core.advertising.Advertiser
import io.github.easynearby.core.connection.Connector
import io.github.easynearby.core.discovery.Discover
import io.github.easynearby.core.loggging.Logger
import timber.log.Timber

internal class AndroidEasyNearbyInitializerFactory : EasyNearbyInitializerFactory {
    override fun createPermissionsChecker(): PermissionsChecker {
        return AndroidPermissionsChecker(nearbyConnectionManager.context)
    }

    override fun createAdvertiseManager(): Advertiser {
        return nearbyConnectionManager.advertiser
    }

    override fun createConnector(): Connector {
        return nearbyConnectionManager.connector
    }

    override fun createDiscover(): Discover {
        return nearbyConnectionManager.discover
    }

    override fun createLogger(): Logger {
        return object : Logger {
            override fun logD(tag: String, msg: String, throwable: Throwable?) {
                Timber.tag(tag).d(throwable, msg)
            }

            override fun logI(tag: String, msg: String, throwable: Throwable?) {
                Timber.tag(tag).i(throwable, msg)
            }

            override fun logE(tag: String, msg: String, throwable: Throwable?) {
                Timber.tag(tag).e(throwable, msg)
            }

            override fun logW(tag: String, msg: String, throwable: Throwable?) {
                Timber.tag(tag).w(throwable, msg)
            }
        }
    }
}