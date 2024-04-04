package com.changeworld.android_easynearby.impl

import android.content.Context
import android.util.Log
import com.changeworld.android_easynearby.logging.AndroidEasyNearby
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

internal class AndroidNearbyConnectionManager(internal val context: Context) {
    private val scope = CoroutineScope(SupervisorJob())
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val connectionEventsMutableFlow = MutableSharedFlow<ConnectionEvents>()

    private val TAG =
        AndroidEasyNearby::class.java.simpleName + " - " + AndroidNearbyConnectionManager::class.java.simpleName

    val advertiser: AndroidAdvertiser by lazy {
        AndroidAdvertiser(
            scope, connectionsClient, connectionLifecycleCallback, connectionEventsMutableFlow
        )
    }

    val connector: AndroidConnector by lazy {
        AndroidConnector(
            scope,
            connectionsClient,
            connectionLifecycleCallback,
            connectionEventsMutableFlow
        )
    }

    val discover: AndroidDiscover by lazy {
        AndroidDiscover(scope, connectionsClient)
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpoint: String, connectionInfo: ConnectionInfo) {
            Log.d(
                TAG,
                "onConnectionInitiated: $endpoint. info: ${connectionInfo.toReadableString()}"
            )
            scope.launch {
                connectionEventsMutableFlow.emit(
                    ConnectionEvents.ConnectionInitiated(
                        endpoint, connectionInfo
                    )
                )
            }
        }

        override fun onConnectionResult(endpoint: String, result: ConnectionResolution) {
            Log.d(
                TAG,
                "onConnectionResult: $endpoint. result: ${result.toReadableString()}"
            )
            scope.launch {
                connectionEventsMutableFlow.emit(
                    ConnectionEvents.ConnectionResult(
                        endpoint,
                        result
                    )
                )
            }
        }

        override fun onDisconnected(endpoint: String) {
            Log.d(TAG, "onDisconnected: endpoint =  $endpoint")
            scope.launch {
                connectionEventsMutableFlow.emit(ConnectionEvents.Disconnected(endpoint))
            }
        }
    }

    private fun ConnectionInfo.toReadableString(): String {
        return "name : $endpointName, info : ${endpointInfo.contentToString()}, isIncomingConnection : $isIncomingConnection"
    }

    private fun ConnectionResolution.toReadableString(): String {
        return "ConnectionResolution(status: Status(isSuccess =    ${status.isSuccess}, statusMessage = ${status.statusMessage}, status = ${status.status}, isCanceled = ${status.isCanceled}, statusCode = ${status.statusCode}, connectionResult = ${status.connectionResult}))"
    }
}