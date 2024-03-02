package com.changeworld.android_easynearby

import android.util.Log
import com.changeworld.easynearby.EasyNearbyInitializerFactory
import com.changeworld.easynearby.PermissionsChecker
import com.changeworld.easynearby.advertising.Advertiser
import com.changeworld.easynearby.connection.Connector
import com.changeworld.easynearby.discovery.Discover
import com.changeworld.easynearby.loggging.Logger

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
                Log.d(tag, msg, throwable)
            }

            override fun logI(tag: String, msg: String, throwable: Throwable?) {
                Log.i(tag, msg, throwable)
            }

            override fun logE(tag: String, msg: String, throwable: Throwable?) {
                Log.e(tag, msg, throwable)
            }

            override fun logW(tag: String, msg: String, throwable: Throwable?) {
                Log.w(tag, msg, throwable)
            }
        }
    }
}