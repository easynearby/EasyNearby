package com.changeworld.easynearby.discovery

import com.changeworld.easynearby.advertising.DeviceInfo
import com.changeworld.easynearby.connection.ConnectionCandidateEvent
import kotlinx.coroutines.flow.Flow

interface Discover {
    /**
     * Starts discovery for remote endpoints with the specified [deviceInfo].
     *
     */
    suspend fun startDiscovery(deviceInfo: DeviceInfo):Result<Flow<ConnectionCandidateEvent>>

    /**
     * Stops discovery for remote endpoints, after a previous call to [startDiscovery], when the client no longer needs to discover endpoints or goes inactive. Payloads can still be sent to connected endpoints after discovery ends.
     */
    fun stopDiscovery()
}