package com.changeworld.easynearby.connection

interface Connector {
    /**
     * Establishes a connection with a remote endpoint.
     * @param endpoint remote endpoint
     * @param name name of the remote endpoint
     * @param isIncomingConnection true if connection is incoming, false if the initiator is the current device
     */
    suspend fun connect(
        endpoint: String,
        name: String,
        isIncomingConnection: Boolean
    ): Result<Connection>

    /**
     * Disconnects from a remote endpoint
     */
    suspend fun disconnect(endpoint: String)

    /**
     * Sends a [payload] to a remote endpoint
     */
    suspend fun sendPayload(endpoint: String, payload: ByteArray): Result<Unit>
}