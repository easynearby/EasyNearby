package com.changeworld.easynearby.advertising

import com.changeworld.easynearby.EasyNearby
import com.changeworld.easynearby.PermissionsChecker
import com.changeworld.easynearby.connection.ConnectionCandidateEvent
import com.changeworld.easynearby.exceptions.PermissionsNotGrantedException
import com.changeworld.easynearby.loggging.extensions.logD
import com.changeworld.easynearby.loggging.extensions.logW
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A thread safe Manager that can start and stop advertising.
 * If permissions not granted returns [PermissionsNotGrantedException] result.
 * If advertising already started returns [IllegalStateException]
 */
class AdvertiseManager(
    private val permissionsChecker: PermissionsChecker,
    private val advertiser: Advertiser
) {

    private val TAG =
        EasyNearby::class.java.simpleName + " - " + AdvertiseManager::class.java.simpleName

    private val isAdvertising = AtomicBoolean(false)

    suspend fun startAdvertising(deviceInfo: DeviceInfo): Result<Flow<ConnectionCandidateEvent>> {
        if (isAdvertising.getAndSet(true)) {
            return Result.failure(IllegalStateException("Already advertising"))
        }

        if (permissionsChecker.hasAllPermissions().not()) {
            return Result.failure(PermissionsNotGrantedException(permissionsChecker.getMissingPermissions()))
        }

        logD(TAG, "Preparing advertising for $deviceInfo")

        return advertiser.startAdvertising(deviceInfo).also {
            isAdvertising.set(it.isSuccess)
        }
    }

    suspend fun stopAdvertising() {
        if (isAdvertising.getAndSet(false)) {
            logD(TAG, "Stopping advertising")
            advertiser.stopAdvertising()
        } else {
            logW(TAG, "Not advertising")
        }
    }
}