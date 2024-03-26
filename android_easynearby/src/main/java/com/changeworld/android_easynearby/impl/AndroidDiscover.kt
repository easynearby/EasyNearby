package com.changeworld.android_easynearby.impl

import android.util.Log
import com.changeworld.easynearby.advertising.DeviceInfo
import com.changeworld.easynearby.connection.ConnectionCandidate
import com.changeworld.easynearby.connection.ConnectionCandidateEvent
import com.changeworld.easynearby.connection.ConnectionEventType
import com.changeworld.easynearby.discovery.Discover
import com.changeworld.android_easynearby.logging.AndroidEasyNearby
import com.changeworld.android_easynearby.toStrategy
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class AndroidDiscover(
    private val scope: CoroutineScope,
    private val connectionsClient: ConnectionsClient,
) : Discover {

    private val TAG =
        AndroidEasyNearby::class.java.simpleName + " - " + AndroidDiscover::class.java.simpleName

    private val discoveredConnectionCandidates = mutableMapOf<String, ConnectionCandidate>()

    private val connectionCandidateMutableSharedFlow = MutableSharedFlow<ConnectionCandidateEvent>()


    override suspend fun startDiscovery(deviceInfo: DeviceInfo): Result<Flow<ConnectionCandidateEvent>> {
        val discoveryOptions = DiscoveryOptions.Builder()
        discoveryOptions.setStrategy(deviceInfo.strategy.toStrategy())
        val exception = suspendCancellableCoroutine { continuation ->
            connectionsClient.startDiscovery(
                deviceInfo.serviceId,
                discoveryCallback,
                discoveryOptions.build()
            ).addOnSuccessListener {
                Log.d(TAG, "Started discovery with $deviceInfo")
                continuation.resume(null)
            }.addOnFailureListener {
                Log.w(TAG, "Failed to start discovery with $deviceInfo", it)
                continuation.resume(it)
            }
        }

        return if (exception == null) {
            Result.success(connectionCandidateMutableSharedFlow)
        } else {
            Result.failure(exception)
        }
    }

    override fun stopDiscovery() {
        connectionsClient.stopDiscovery()
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "onEndpointFound. endpointId: $endpointId, info: ${info.toReadableString()}")
            val connectionCandidate = ConnectionCandidate(endpointId, info.endpointName, null)
            discoveredConnectionCandidates[endpointId] = connectionCandidate
            scope.launch {
                connectionCandidateMutableSharedFlow.emit(
                    ConnectionCandidateEvent(
                        ConnectionEventType.DISCOVERED,
                        connectionCandidate
                    )
                )
            }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "OnEndpointLost. endpointId: $endpointId")
            discoveredConnectionCandidates.remove(endpointId)?.let { connectionCandidate ->
                scope.launch {
                    connectionCandidateMutableSharedFlow.emit(
                        ConnectionCandidateEvent(
                            ConnectionEventType.LOST,
                            connectionCandidate
                        )
                    )
                }
            }
        }
    }

    private fun DiscoveredEndpointInfo.toReadableString(): String {
        return "name : $endpointName, endpointInfo : ${this.endpointInfo.contentToString()}"
    }
}