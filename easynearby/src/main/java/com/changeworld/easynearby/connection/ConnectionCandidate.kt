package com.changeworld.easynearby.connection

import com.changeworld.easynearby.EasyNearby
import com.changeworld.easynearby.di.IsolatedKoinContext
import com.changeworld.easynearby.loggging.extensions.logD

/**
 * Connection candidate that represents a remote device. [isIncomingConnection] is used to distinguish
 * between connections intitiated by this app and connections initiated by other apps.
 */
data class ConnectionCandidate(
    val id: String,
    val name: String,
    val isIncomingConnection: Boolean
) {
    private val connectionManager: ConnectionManager by lazy {
        IsolatedKoinContext.koin.get()
    }

    private val TAG =
        EasyNearby::class.java.simpleName + " - " + ConnectionCandidate::class.java.simpleName

    /**
     * Tries to connect to the remote endpoint and returns a [Connection] if result is successful
     */
    suspend fun connect(): Result<Connection> {
        logD(TAG, "Connecting to $id")
        return connectionManager.connect(id, name, isIncomingConnection)
    }
}