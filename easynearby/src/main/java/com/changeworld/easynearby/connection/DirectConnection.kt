package com.changeworld.easynearby.connection

import com.changeworld.easynearby.di.IsolatedKoinContext
import kotlinx.coroutines.flow.Flow

/**
 * Direct implementation of [Connection].
 */
class DirectConnection(override val id: String, override val name: String, private val receiveFlow: Flow<ByteArray>) :
    Connection {

    private val connectionManager: ConnectionManager by lazy {
        IsolatedKoinContext.koin.get()
    }

    override suspend fun sendPayload(payload: ByteArray): Result<Unit> {
        return connectionManager.sendPayload(id, payload)
    }

    override suspend fun getPayload(): Flow<ByteArray> {
        return receiveFlow
    }

    override suspend fun close() {
        connectionManager.disconnect(id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DirectConnection

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}