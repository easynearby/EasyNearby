package io.github.easynearby.android

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import io.github.easynearby.core.EasyNearby
import io.github.easynearby.android.impl.AndroidNearbyConnectionManager
import io.github.easynearby.android.logging.AndroidEasyNearby

@SuppressLint("StaticFieldLeak")
internal lateinit var nearbyConnectionManager: AndroidNearbyConnectionManager

internal class AndroidEasyNearbyInitializer : Initializer<AndroidNearbyConnectionManager> {

    private val TAG =
        AndroidEasyNearby::class.java.simpleName + " - " + AndroidEasyNearbyInitializer::class.java.simpleName

    override fun create(context: Context): AndroidNearbyConnectionManager {
        Log.d(TAG, "initializing " + AndroidNearbyConnectionManager::class.java.simpleName)
        nearbyConnectionManager = AndroidNearbyConnectionManager(context)
        EasyNearby.initialize()
        return nearbyConnectionManager
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}