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
        id: String, name: String, isIncomingConnection: Boolean, authValidator: (String) -> Boolean
    ): Result<Connection> {
        logD(TAG, "Connecting to $id. Incoming: $isIncomingConnection. Name: $name")
        return connector.connect(id, name, isIncomingConnection, authValidator).map {
            GzipConnection(it)
        }
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