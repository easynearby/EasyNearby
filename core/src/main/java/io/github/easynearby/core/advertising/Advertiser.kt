package io.github.easynearby.core.advertising

import io.github.easynearby.core.connection.ConnectionCandidateEvent
import kotlinx.coroutines.flow.Flow


interface Advertiser {

    /**
     * Starts advertising an endpoint for a local app.
     */
    suspend fun startAdvertising(deviceInfo: DeviceInfo): Result<Flow<ConnectionCandidateEvent>>

    /**
     * Stops advertising a local endpoint. Should be called after calling [startAdvertising], as soon as the application no longer needs to advertise itself or goes inactive. Payloads can still be sent to connected endpoints after advertising ends.
     */
    suspend fun stopAdvertising()
}