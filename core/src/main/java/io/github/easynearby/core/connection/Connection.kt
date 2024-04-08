package io.github.easynearby.core.connection

import kotlinx.coroutines.flow.Flow


/**
 * A connection between two devices.
 */
interface Connection {
    val id: String
    val name: String

    /**
     * Sends a [payload] to a remote endpoint
     */
    suspend fun sendPayload(payload: ByteArray): Result<Unit>

    /**
     * Returns a [Flow] of payloads received from a remote endpoint. Once connection is closed the flow will complete
     */
    suspend fun getPayload(): Flow<ByteArray>

    /**
     * Closes the connection
     */
    suspend fun close()
}