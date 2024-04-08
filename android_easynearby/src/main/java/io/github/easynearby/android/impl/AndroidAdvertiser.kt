package io.github.easynearby.android.impl


import android.util.Log
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionsClient
import io.github.easynearby.android.toStrategy
import io.github.easynearby.core.advertising.Advertiser
import io.github.easynearby.core.advertising.DeviceInfo
import io.github.easynearby.core.connection.ConnectionCandidate
import io.github.easynearby.core.connection.ConnectionCandidateEvent
import io.github.easynearby.core.connection.ConnectionEventType
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
                        disconnected(connectionEvent)
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
                Log.d(TAG, "Success to start advertising with $deviceInfo")
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
        if (connectionResult.result.status.isSuccess.not()) {
            val connectionCandidate = connectionCandidates.remove(connectionResult.endpoint)
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

            if (connectionCandidates.containsKey(initiated.endpoint).not()) {
                connectionCandidates[initiated.endpoint] = connectionCandidate
                scope.launch {
                    connectionCandidateMutableSharedFlow.emit(
                        ConnectionCandidateEvent(
                            ConnectionEventType.DISCOVERED,
                            connectionCandidate
                        )
                    )
                }
            } else {
                Log.d(
                    TAG,
                    "connection $connectionCandidate initiated connection but has been already discovered"
                )
            }
        }
    }

    private fun disconnected(disconnectedEvent: ConnectionEvents.Disconnected) {
        val connectionCandidate = connectionCandidates.remove(disconnectedEvent.endpoint)
        connectionCandidate?.let {
            scope.launch {
                connectionCandidateMutableSharedFlow.emit(
                    ConnectionCandidateEvent(ConnectionEventType.LOST, connectionCandidate)
                )
            }
        }
    }


    override suspend fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }
}