package io.github.easynearby.core.advertising

import io.github.easynearby.core.EasyNearby
import io.github.easynearby.core.PermissionsChecker
import io.github.easynearby.core.connection.ConnectionCandidateEvent
import io.github.easynearby.core.exceptions.PermissionsNotGrantedException
import io.github.easynearby.core.loggging.extensions.logD
import io.github.easynearby.core.loggging.extensions.logW
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

    /**
     * Starts advertising a local endpoint with the specified [deviceInfo].
     * If advertising already started returns [IllegalStateException]
     * If permissions not granted returns [PermissionsNotGrantedException]
     * Otherwise returns [Flow] of [ConnectionCandidateEvent]
     */
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

    /**
     * Stops advertising
     */
    suspend fun stopAdvertising() {
        if (isAdvertising.getAndSet(false)) {
            logD(TAG, "Stopping advertising")
            advertiser.stopAdvertising()
        } else {
            logW(TAG, "Not advertising")
        }
    }
}