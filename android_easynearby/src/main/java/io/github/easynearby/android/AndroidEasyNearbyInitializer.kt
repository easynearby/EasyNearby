package io.github.easynearby.android

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.startup.Initializer
import io.github.easynearby.android.impl.AndroidNearbyConnectionManager
import io.github.easynearby.android.logging.AndroidEasyNearby
import io.github.easynearby.android.logging.CrashReportingTree
import io.github.easynearby.core.EasyNearby
import timber.log.Timber

@SuppressLint("StaticFieldLeak")
internal lateinit var nearbyConnectionManager: AndroidNearbyConnectionManager

internal class AndroidEasyNearbyInitializer : Initializer<AndroidNearbyConnectionManager> {

    private val TAG =
        AndroidEasyNearby::class.java.simpleName + " - " + AndroidEasyNearbyInitializer::class.java.simpleName


    @SuppressLint("LogNotTimber")
    override fun create(context: Context): AndroidNearbyConnectionManager {
        if (context.isLoggingEnabled()) {
            Timber.plant(Timber.DebugTree())
        } else {
            Log.i(
                TAG,
                "To enable logging, add <meta-data android:name=\"io.github.easynearby.android.logging\" android:value=\"true\" /> to your app's AndroidManifest.xml"
            )
            Timber.plant(CrashReportingTree())
        }
        Timber.tag(TAG).d("initializing %s", AndroidNearbyConnectionManager::class.java.simpleName)
        nearbyConnectionManager = AndroidNearbyConnectionManager(context)
        EasyNearby.initialize()
        return nearbyConnectionManager
    }

    private fun Context.isLoggingEnabled(): Boolean = packageManager.getApplicationInfo(
        packageName,
        PackageManager.GET_META_DATA
    ).metaData.getBoolean("io.github.easynearby.android.logging", false)


    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}