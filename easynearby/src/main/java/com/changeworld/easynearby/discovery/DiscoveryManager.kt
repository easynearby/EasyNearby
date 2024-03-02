package com.changeworld.easynearby.discovery

import com.changeworld.easynearby.EasyNearby
import com.changeworld.easynearby.PermissionsChecker
import com.changeworld.easynearby.advertising.DeviceInfo
import com.changeworld.easynearby.connection.ConnectionCandidateEvent
import com.changeworld.easynearby.exceptions.PermissionsNotGrantedException
import com.changeworld.easynearby.loggging.extensions.logD
import com.changeworld.easynearby.loggging.extensions.logW
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A thread safe Manager that can start and stop discovery.
 * If permissions not granted returns [PermissionsNotGrantedException] result.
 * If discovery already started returns [IllegalStateException]
 */
class DiscoveryManager(
    private val permissionsChecker: PermissionsChecker,
    private val discover: Discover
) {
    private val TAG =
        EasyNearby::class.java.simpleName + " - " + DiscoveryManager::class.java.simpleName

    private val isDiscovering = AtomicBoolean(false)

    suspend fun startDiscovery(deviceInfo: DeviceInfo): Result<Flow<ConnectionCandidateEvent>> {
        if (isDiscovering.getAndSet(true)) {
            return Result.failure(IllegalStateException("Already Discovering"))
        }

        if (permissionsChecker.hasAllPermissions().not()) {
            return Result.failure(PermissionsNotGrantedException(permissionsChecker.getMissingPermissions()))
        }

        logD(TAG, "Preparing discovery for $deviceInfo")

        return discover.startDiscovery(deviceInfo).also {
            isDiscovering.set(it.isSuccess)
        }
    }

    fun stopDiscovery() {
        if (isDiscovering.getAndSet(false)) {
            logD(TAG, "Stopping discovery")
            discover.stopDiscovery()
        } else {
            logW(TAG, "Not Discovering")
        }
    }
}