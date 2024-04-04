package com.changeworld.android_easynearby.impl

import android.util.Log
import com.changeworld.android_easynearby.logging.AndroidEasyNearby
import com.changeworld.easynearby.connection.Connection
import com.changeworld.easynearby.connection.Connector
import com.changeworld.easynearby.connection.DirectConnection
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class AndroidConnector(
    private val scope: CoroutineScope,
    private val connectionsClient: ConnectionsClient,
    private val connectionLifecycleCallback: ConnectionLifecycleCallback,
    private val connectionEventsFlow: Flow<ConnectionEvents>
) : Connector {

    private val TAG =
        AndroidEasyNearby::class.java.simpleName + " - " + AndroidConnector::class.java.simpleName

    private val mapOfSendChannels =
        mutableMapOf<String, SendChannel<ByteArray>>()

    private val pendingConnections =
        mutableMapOf<String, PendingConnectionParams>()

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

    override suspend fun connect(
        endpoint: String,
        localDeviceName: String,
        remoteDeviceName: String,
        isIncomingConnection: Boolean,
        authValidator: suspend (String) -> Boolean
    ): Result<Connection> {
        return if (isIncomingConnection) {
            acceptConnection(endpoint,remoteDeviceName, authValidator)
        } else {
            connectToEndpoint(endpoint, localDeviceName, remoteDeviceName,authValidator)
        }
    }

    override suspend fun disconnect(endpoint: String) {
        connectionsClient.disconnectFromEndpoint(endpoint).also {
            Log.d(TAG, "Disconnected from $endpoint")
            mapOfSendChannels.remove(endpoint)?.also {
                it.close()
            }
        }
    }

    override suspend fun rejectConnection(endpoint: String) {
        connectionsClient.rejectConnection(endpoint).addOnSuccessListener {
            Log.d(TAG, "Rejected connection from $endpoint")
        }.addOnFailureListener {
            Log.w(TAG, "Failed to reject connection from $endpoint", it)
        }
    }

    override suspend fun sendPayload(endpoint: String, payload: ByteArray): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            Log.d(TAG, "Sending payload $payload to $endpoint")
            connectionsClient.sendPayload(endpoint, Payload.fromBytes(payload))
                .addOnSuccessListener {
                    Log.d(TAG, "Sent payload $payload to $endpoint")
                    continuation.resume(Result.success(Unit))
                }.addOnFailureListener {
                    Log.w(TAG, "Failed to send payload $payload to $endpoint", it)
                    continuation.resume(Result.failure(it))
                }
        }
    }

    private fun disconnected(connectionEvent: ConnectionEvents.Disconnected) {
        mapOfSendChannels.remove(connectionEvent.endpoint)?.close()?.also {
            Log.d(TAG, "Disconnected from ${connectionEvent.endpoint}")
        }
    }

    private fun connectionInitiated(connectionEvent: ConnectionEvents.ConnectionInitiated) {
        if (connectionEvent.connectionInfo.isIncomingConnection.not()) {
            val connectionEventsParams = pendingConnections[connectionEvent.endpoint] ?: run {
                Log.w(TAG, "Could not find pending connection for ${connectionEvent.endpoint}")
                return
            }
            scope.launch {
                val isAuthenticationValidated =
                    connectionEventsParams.authValidator(connectionEvent.connectionInfo.authenticationDigits)
                if (isAuthenticationValidated) {
                    Log.d(
                        TAG,
                        "Accepting outgoing connection from ${connectionEvent.endpoint}. Validation result: $isAuthenticationValidated"
                    )
                    acceptOutgoingConnectionRequest(connectionEvent.endpoint)
                } else {
                    Log.d(
                        TAG,
                        "Rejecting outgoing connection from ${connectionEvent.endpoint}. Validation result: $isAuthenticationValidated"
                    )
                    rejectConnection(connectionEvent.endpoint)
                }
            }
        }
    }

    private fun connectionResult(connectionEvent: ConnectionEvents.ConnectionResult) {
        pendingConnections.remove(connectionEvent.endpoint)?.let {
            if (connectionEvent.result.status.isSuccess) {
                Log.d(TAG, "Success to connect to ${connectionEvent.endpoint}")
                Log.d(TAG, "Initializing receive channel for ${connectionEvent.endpoint}")
                val sendChannel = Channel<ByteArray>()
                mapOfSendChannels[connectionEvent.endpoint] = sendChannel
                it.continuation.resume(
                    Result.success(
                        DirectConnection(
                            it.id,
                            it.name,
                            sendChannel.receiveAsFlow()
                        )
                    )
                )
            } else {
                Log.d(TAG, "Failed to connect to ${connectionEvent.endpoint} with ${connectionEvent.result.status.statusMessage}")
                it.continuation.resume(Result.failure(RuntimeException("Failed to connect. ${connectionEvent.result.status.statusMessage}")))
            }
        }
    }

    private suspend fun connectToEndpoint(
        endpoint: String,
        localDeviceName: String,
        remoteDeviceName: String,
        authValidator: suspend (String) -> Boolean
    ): Result<Connection> {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                pendingConnections.remove(endpoint)
            }
            pendingConnections[endpoint] =
                PendingConnectionParams(endpoint, remoteDeviceName, continuation, authValidator)
            connectionsClient.requestConnection(localDeviceName, endpoint, connectionLifecycleCallback)
                .addOnSuccessListener {
                    Log.d(TAG, "Requested connection to $endpoint successfully executed")
                }.addOnFailureListener {
                    Log.w(TAG, "Failed to request connection to $endpoint", it)
                    pendingConnections.remove(endpoint)
                    continuation.resume(Result.failure(it))
                }
        }
    }


    private suspend fun acceptConnection(
        endpoint: String,
        remoteDeviceName: String,
        authValidator: suspend (String) -> Boolean
    ): Result<Connection> {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                pendingConnections.remove(endpoint)
            }
            pendingConnections[endpoint] =
                PendingConnectionParams(endpoint, remoteDeviceName, continuation, authValidator)
            connectionsClient.acceptConnection(endpoint, payloadCallback)
                .addOnSuccessListener {
                    Log.d(TAG, "Accepted connection success")
                }.addOnFailureListener {
                    Log.w(TAG, "Failed to accept connection", it)
                    pendingConnections.remove(endpoint)
                    continuation.resume(Result.failure(it))
                }
        }
    }

    private fun acceptOutgoingConnectionRequest(endpoint: String) {
        connectionsClient.acceptConnection(endpoint, payloadCallback).addOnSuccessListener {
            Log.d(TAG, "Accepted outgoing connection successfully executed")
        }.addOnFailureListener {
            Log.w(TAG, "Failed to accept outgoing connection", it)
            pendingConnections.remove(endpoint)?.continuation?.resume(Result.failure(it))
        }
    }


    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.d(TAG, "OnPayloadReceived. endpointId: $endpointId, payload: $payload")
            payload.asBytes()?.let { data ->
                scope.launch {
                    mapOfSendChannels[endpointId]?.send(data)
                }
            }

        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Log.d(TAG, "OnPayloadTransferUpdate")
        }
    }
}

internal data class PendingConnectionParams(
    val id: String,
    val name: String,
    val continuation: CancellableContinuation<Result<Connection>>,
    val authValidator: suspend (String) -> Boolean,
)