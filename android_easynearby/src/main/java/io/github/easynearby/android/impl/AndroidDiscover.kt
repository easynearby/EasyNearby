package io.github.easynearby.android.impl

import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import io.github.easynearby.android.logging.AndroidEasyNearby
import io.github.easynearby.android.toStrategy
import io.github.easynearby.core.advertising.DeviceInfo
import io.github.easynearby.core.connection.ConnectionCandidate
import io.github.easynearby.core.connection.ConnectionCandidateEvent
import io.github.easynearby.core.connection.ConnectionEventType
import io.github.easynearby.core.discovery.Discover
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
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
                Timber.tag(TAG).d("Started discovery with %s", deviceInfo)
                continuation.resume(null)
            }.addOnFailureListener {
                Timber.tag(TAG).w(it, "Failed to start discovery with %s", deviceInfo)
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
            Timber.tag(TAG)
                .d("onEndpointFound. endpointId: %s, info %s", endpointId, info.toReadableString())
            val connectionCandidate = ConnectionCandidate(endpointId, info.endpointName, null)
            if (discoveredConnectionCandidates.containsKey(endpointId).not()) {
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
        }

        override fun onEndpointLost(endpointId: String) {
            Timber.tag(TAG).d("OnEndpointLost. endpointId: %s", endpointId)
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