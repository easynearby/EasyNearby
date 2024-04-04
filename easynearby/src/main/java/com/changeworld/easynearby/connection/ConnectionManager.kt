package com.changeworld.easynearby.connection

import com.changeworld.easynearby.EasyNearby
import com.changeworld.easynearby.connection.gzip.GzipConnection
import com.changeworld.easynearby.loggging.extensions.logD

/**
 * Manager for connections. It's responsible for managing connections and sending payloads.
 */
internal class ConnectionManager(
    private val connector: Connector
) {
    private val TAG =
        EasyNearby::class.java.simpleName + " - " + ConnectionManager::class.java.simpleName

    suspend fun connect(
        id: String, localDevicename: String, remoteDeviceName:String, isIncomingConnection: Boolean, authValidator: suspend (String) -> Boolean
    ): Result<Connection> {
        logD(TAG, "Connecting to $id. Incoming: $isIncomingConnection. Name: $localDevicename")
        return connector.connect(id, localDevicename, remoteDeviceName, isIncomingConnection, authValidator).map {
            GzipConnection(it)
        }
    }

    suspend fun rejectConnection(id: String) {
        logD(TAG, "Rejecting connection to $id")
        connector.rejectConnection(id)
    }

    suspend fun disconnect(id: String) {
        logD(TAG, "Disconnecting from $id")
        connector.disconnect(id)
    }

    suspend fun sendPayload(id: String, payload: ByteArray): Result<Unit> {
        logD(TAG, "Sending payload $payload to $id")
        return connector.sendPayload(id, payload)
    }
}