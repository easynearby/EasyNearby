package com.changeworld.android_easynearby.impl


import android.util.Log
import com.changeworld.easynearby.advertising.Advertiser
import com.changeworld.easynearby.advertising.DeviceInfo
import com.changeworld.easynearby.connection.ConnectionCandidate
import com.changeworld.easynearby.connection.ConnectionCandidateEvent
import com.changeworld.easynearby.connection.ConnectionEventType
import com.changeworld.android_easynearby.toStrategy
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionsClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class AndroidAdvertiser(
    private val scope: CoroutineScope,
    private val connectionsClient: ConnectionsClient,
    private val connectionLifecycleCallback: ConnectionLifecycleCallback,
    private val connectionEventsFlow: Flow<ConnectionEvents>
) : Advertiser {

    init {
        scope.launch {
            connectionEventsFlow.collect { connectionEvent ->
                when (connectionEvent) {
                    is ConnectionEvents.ConnectionInitiated -> {
                        connectionInitiated(connectionEvent)
                    }

                    is ConnectionEvents.ConnectionResult -> {
                        connectionResult(connectionEvent)
                    }

                    is ConnectionEvents.Disconnected -> {
                        // Not intrested
                    }
                }
            }
        }
    }

    private val TAG =
        AndroidAdvertiser::class.java.simpleName + " - " + AndroidAdvertiser::class.java.simpleName

    private val connectionCandidates = mutableMapOf<String, ConnectionCandidate>()

    private val connectionCandidateMutableSharedFlow =
        MutableSharedFlow<ConnectionCandidateEvent>()

    override suspend fun startAdvertising(deviceInfo: DeviceInfo): Result<Flow<ConnectionCandidateEvent>> {
        val advertisingOptions = AdvertisingOptions.Builder()
        advertisingOptions.setStrategy(deviceInfo.strategy.toStrategy())
        val exception = suspendCancellableCoroutine { continuation ->
            connectionsClient.startAdvertising(
                deviceInfo.name,
                deviceInfo.serviceId,
                connectionLifecycleCallback,
                advertisingOptions.build()
            ).addOnSuccessListener {
                Log.d(TAG, "Success to start advertising")
                continuation.resume(null)
            }.addOnFailureListener {
                Log.e(TAG, "Failed to start advertising", it)
                continuation.resume(it)
            }
        }
        return if (exception == null) {
            Result.success(connectionCandidateMutableSharedFlow)
        } else {
            Result.failure(exception)
        }
    }

    private fun connectionResult(connectionResult: ConnectionEvents.ConnectionResult) {
        val connectionCandidate = connectionCandidates.remove(connectionResult.endpoint)
        if (connectionResult.result.status.isSuccess.not()) {
            connectionCandidate?.let {
                scope.launch {
                    connectionCandidateMutableSharedFlow.emit(
                        ConnectionCandidateEvent(ConnectionEventType.LOST, connectionCandidate)
                    )
                }
            }
        }
    }

    private fun connectionInitiated(initiated: ConnectionEvents.ConnectionInitiated) {
        if (initiated.connectionInfo.isIncomingConnection) {
            val connectionCandidate = ConnectionCandidate(
                initiated.endpoint,
                initiated.connectionInfo.endpointName,
                initiated.connectionInfo.authenticationDigits
            )
            connectionCandidates[initiated.endpoint] = connectionCandidate
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

    override suspend fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }
}