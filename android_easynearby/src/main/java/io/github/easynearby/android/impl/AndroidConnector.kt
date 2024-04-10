package io.github.easynearby.android.impl

import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import io.github.easynearby.android.logging.AndroidEasyNearby
import io.github.easynearby.core.connection.Connection
import io.github.easynearby.core.connection.Connector
import io.github.easynearby.core.connection.DirectConnection
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
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
            acceptConnection(endpoint, remoteDeviceName, authValidator)
        } else {
            connectToEndpoint(endpoint, localDeviceName, remoteDeviceName, authValidator)
        }
    }

    override suspend fun disconnect(endpoint: String) {
        connectionsClient.disconnectFromEndpoint(endpoint).also {
            Timber.tag(TAG).d("Disconnected from %s", endpoint)
            mapOfSendChannels.remove(endpoint)?.also {
                it.close()
            }
        }
    }

    override suspend fun rejectConnection(endpoint: String) {
        connectionsClient.rejectConnection(endpoint).addOnSuccessListener {
            Timber.tag(TAG).d("Rejected connection from %s", endpoint)
        }.addOnFailureListener {
            Timber.tag(TAG).w(it, "Failed to reject connection from %s", endpoint)
        }
    }

    override suspend fun sendPayload(endpoint: String, payload: ByteArray): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            Timber.tag(TAG).d("Sending payload %s to %s", payload, endpoint)
            connectionsClient.sendPayload(endpoint, Payload.fromBytes(payload))
                .addOnSuccessListener {
                    Timber.tag(TAG).d("Sent payload %s to %s", payload, endpoint)
                    continuation.resume(Result.success(Unit))
                }.addOnFailureListener {
                    Timber.tag(TAG).w(it, "Failed to send payload %s to %s ", payload, endpoint)
                    continuation.resume(Result.failure(it))
                }
        }
    }

    private fun disconnected(connectionEvent: ConnectionEvents.Disconnected) {
        mapOfSendChannels.remove(connectionEvent.endpoint)?.close()?.also {
            Timber.tag(TAG).d("Disconnected from %s", connectionEvent.endpoint)
        }
    }

    private fun connectionInitiated(connectionEvent: ConnectionEvents.ConnectionInitiated) {
        if (connectionEvent.connectionInfo.isIncomingConnection.not()) {
            val connectionEventsParams = pendingConnections[connectionEvent.endpoint] ?: run {
                Timber.tag(TAG)
                    .w("Could not find pending connection for %s", connectionEvent.endpoint)
                return
            }
            scope.launch {
                val isAuthenticationValidated =
                    connectionEventsParams.authValidator(connectionEvent.connectionInfo.authenticationDigits)
                if (isAuthenticationValidated) {
                    Timber.tag(TAG)
                        .d(
                            "Accepting outgoing connection from %s",
                            connectionEvent.endpoint
                        )
                    acceptOutgoingConnectionRequest(connectionEvent.endpoint)
                } else {
                    Timber.tag(TAG)
                        .d("Rejecting outgoing connection from %s", connectionEvent.endpoint)
                    rejectConnection(connectionEvent.endpoint)
                }
            }
        }
    }

    private fun connectionResult(connectionEvent: ConnectionEvents.ConnectionResult) {
        pendingConnections.remove(connectionEvent.endpoint)?.let {
            if (connectionEvent.result.status.isSuccess) {
                Timber.tag(TAG).d("Success to connect to %s", connectionEvent.endpoint)
                Timber.tag(TAG).d("Initializing receive channel for %s", connectionEvent.endpoint)
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
                Timber.tag(TAG)
                    .d(
                        "Failed to connect to %s with %s",
                        connectionEvent.endpoint,
                        connectionEvent.result.status.statusMessage
                    )
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
            connectionsClient.requestConnection(
                localDeviceName,
                endpoint,
                connectionLifecycleCallback
            )
                .addOnSuccessListener {
                    Timber.tag(TAG)
                        .d("Requested connection to  %s successfully executed", endpoint)
                }.addOnFailureListener {
                    Timber.tag(TAG).w(it, "Failed to request connection to %s", endpoint)
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
                    Timber.tag(TAG).d("Accepted connection success")
                }.addOnFailureListener {
                    Timber.tag(TAG).w(it, "Failed to accept connection")
                    pendingConnections.remove(endpoint)
                    continuation.resume(Result.failure(it))
                }
        }
    }

    private fun acceptOutgoingConnectionRequest(endpoint: String) {
        connectionsClient.acceptConnection(endpoint, payloadCallback).addOnSuccessListener {
            Timber.tag(TAG).d("Accepted outgoing connection successfully executed")
        }.addOnFailureListener {
            Timber.tag(TAG).w(it, "Failed to accept outgoing connection")
            pendingConnections.remove(endpoint)?.continuation?.resume(Result.failure(it))
        }
    }


    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Timber.tag(TAG).d("OnPayloadReceived. endpointId: %s, payload %s", endpointId, payload)
            payload.asBytes()?.let { data ->
                scope.launch {
                    mapOfSendChannels[endpointId]?.send(data)
                }
            }

        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Timber.tag(TAG).d("OnPayloadTransferUpdate")
        }
    }
}

internal data class PendingConnectionParams(
    val id: String,
    val name: String,
    val continuation: CancellableContinuation<Result<Connection>>,
    val authValidator: suspend (String) -> Boolean,
)