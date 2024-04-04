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
    /**
     * In case of incoming connections, this data is already available. If the connection is outgoing the data will be available later
     */
    internal val authenticationDigits: String?,
) {
    /**
     * Authentication digits are only available for incoming connections
     */
    val isIncomingConnection: Boolean = authenticationDigits != null

    private val connectionManager: ConnectionManager by lazy {
        IsolatedKoinContext.koin.get()
    }

    private val TAG =
        EasyNearby::class.java.simpleName + " - " + ConnectionCandidate::class.java.simpleName

    /**
     * Tries to connect to the remote endpoint and returns a [Connection] if result is successful
     * @param localDeviceName name of the caller that tries to connect
     * @param authValidator optional auth validator. If not provided, all connections are accepted,
     * otherwise the caller will be able to choose whether to accept or reject the connection.
     */
    suspend fun connect(
        localDeviceName: String,
        authValidator: suspend (String) -> Boolean = { true }
    ): Result<Connection> {
        logD(TAG, "Connecting to $id")
        return authenticationDigits?.let {
            if (authValidator(it)) {
                connectionManager.connect(id, localDeviceName,name, isIncomingConnection, authValidator)
            } else {
                connectionManager.rejectConnection(id)
                logD(TAG, "Connection rejected")
                Result.failure(RuntimeException("Connection rejected because authValidator returned false"))
            }
        } ?: connectionManager.connect(id, localDeviceName,name, isIncomingConnection, authValidator)
    }
}