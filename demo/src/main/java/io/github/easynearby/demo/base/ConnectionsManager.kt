package io.github.easynearby.demo.base

import io.github.easynearby.core.connection.Connection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.util.Date

object ConnectionsManager {
    private var messageId = 0
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val connections = mutableListOf<Connection>()

    private val _connectionManagerState = MutableStateFlow<ConnectionsManagerState?>(null)
    val connectionManagerState = _connectionManagerState.asStateFlow()

    private val communicationMessages = mutableMapOf<String, MutableList<Message>>()
    private val subscribedMessageListener = mutableMapOf<String, MutableSharedFlow<Message>>()

    fun addAndStartListeningForMessages(connection: Connection) {
        connections.add(connection)
        listenForMessages(connection)
    }

    fun getMessagesFor(id: String): List<Message> {
        return communicationMessages[id] ?: emptyList()
    }

    fun sendMessageFor(id: String, messageStr: String) {
        val message =
            Message(messageId++.toString(), Date(), id, messageStr, MessageType.OUTGOING, false)
        communicationMessages.getOrPut(id) { mutableListOf() }.add(message)

        subscribedMessageListener[id]?.let {
            scope.launch {
                it.emit(message)
            }
        }
        scope.launch {
            connections.find { it.id == id }?.sendPayload(message.message.toByteArray())
                ?.onSuccess {
                    val changedMessage = message.copy(isProcessed = true)
                    communicationMessages.getOrPut(id) { mutableListOf() }.replaceAll {
                        if (it.id == message.id) changedMessage else it
                    }

                    subscribedMessageListener[id]?.emit(changedMessage)
                }
        }
    }

    fun receiveMessagesFor(id: String): Flow<Message> = flow {
        val sharedFlow = MutableSharedFlow<Message>(replay = 100, extraBufferCapacity = 100)
        subscribedMessageListener[id] = sharedFlow


        communicationMessages[id]?.let { messages ->
            messages.replaceAll { it.copy(isProcessed = true) }
            messages.forEach { message ->
                sharedFlow.emit(message)
            }
        }

        sharedFlow.collect {
            emit(it)
        }

    }.onCompletion {
        subscribedMessageListener.remove(id)
    }

    fun getConnectionNameFor(id: String): String? {
        return connections.find { it.id == id }?.name
    }

    fun disconnect(id: String) {
        scope.launch {
            connections.find { it.id == id }?.close()
        }
    }

    private fun listenForMessages(connection: Connection) {
        scope.launch {
            connection.getPayload().onCompletion {
                if (connections.remove(connection)) {
                    _connectionManagerState.value =
                        ConnectionsManagerState.Disconnected(connection)
                }
            }.collect { payload ->
                val message =
                    processOnMessageReceived(connection.id, connection.name, String(payload))
                _connectionManagerState.value =
                    ConnectionsManagerState.ReceivedMessage(connection.id, message)
            }
        }
    }

    private fun processOnMessageReceived(
        deviceId: String, name: String, messageStr: String
    ): Message {
        val sharedFlow = subscribedMessageListener[deviceId]
        val message = Message(
            messageId++.toString(),
            Date(),
            name,
            messageStr,
            MessageType.INCOMING,
            sharedFlow != null
        )
        communicationMessages.getOrPut(deviceId) { mutableListOf() }.add(message)

        sharedFlow?.let {
            scope.launch {
                it.emit(message)
            }
        }
        return message
    }

}

sealed class ConnectionsManagerState {
    data class Disconnected(val connection: Connection) : ConnectionsManagerState()

    data class ReceivedMessage(val id: String, val message: Message) : ConnectionsManagerState()
}

data class Message(
    val id: String, val time: Date, val from: String, val message: String, val type: MessageType,
    /**
     * In case of incoming message, this flag will be set to true when the message is viewed.
     * In case of outgoing message, this flag will be set to true when the message is sent.
     */
    var isProcessed: Boolean
)

enum class MessageType {
    INCOMING, OUTGOING
}