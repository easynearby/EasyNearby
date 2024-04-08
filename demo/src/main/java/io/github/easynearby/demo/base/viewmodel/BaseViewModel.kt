package io.github.easynearby.demo.base.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.easynearby.core.advertising.DeviceInfo
import io.github.easynearby.core.connection.Connection
import io.github.easynearby.core.connection.ConnectionCandidate
import io.github.easynearby.core.connection.ConnectionCandidateEvent
import io.github.easynearby.core.connection.ConnectionEventType.DISCOVERED
import io.github.easynearby.core.connection.ConnectionEventType.LOST
import io.github.easynearby.demo.base.ConnectionsManager
import io.github.easynearby.demo.base.ConnectionsManagerState
import io.github.easynearby.demo.base.Message
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

abstract class BaseViewModel : ViewModel() {

    private val TAG = BaseViewModel::class.java.simpleName
    private val _operationState = MutableStateFlow<OperationState?>(null)
    val operationState = _operationState.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState?>(null)
    val connectionState = _connectionState.asStateFlow()

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents = _errorEvents

    private val connectionCandidates = mutableListOf<ConnectionCandidate>()
    private val waitingForAuthAcceptingContinuations = mutableMapOf<String, Continuation<Boolean>>()
    private var listenJob: Job? = null
    private var myDeviceName: String? = null

    init {
        viewModelScope.launch {
            ConnectionsManager.connectionManagerState.collect { state ->
                when (state) {
                    is ConnectionsManagerState.Disconnected -> {
                        _connectionState.value = ConnectionState.Disconnected(state.connection)
                    }

                    is ConnectionsManagerState.ReceivedMessage -> {
                        _connectionState.value =
                            ConnectionState.ReceivedMessage(state.id, state.message)
                    }

                    null -> {
                        // do nothing
                    }

                }

            }
        }
    }

    abstract suspend fun startOperation(deviceInfo: DeviceInfo): Result<Flow<ConnectionCandidateEvent>>
    abstract suspend fun stopOperation()

    fun start(deviceInfo: DeviceInfo) {
        _operationState.value = OperationState.InProgress
        viewModelScope.launch {
            startOperation(deviceInfo)
                .onSuccess {
                    myDeviceName = deviceInfo.name
                    _operationState.value = OperationState.Success
                    listenJob?.cancel()
                    listenJob = listenForConnectionCandidateEvents(it)
                }.onFailure {
                    _operationState.value = OperationState.Failed("Failed to start operation")
                    _errorEvents.emit("Failed to start operation")
                }
        }
    }

    fun stop() {
        viewModelScope.launch {
            stopOperation()
        }
    }

    fun connect(deviceId: String) {
        connectionCandidates.find {
            it.id == deviceId
        }?.let { connectionCandidate ->
            _connectionState.value = ConnectionState.InProgress(connectionCandidate.id)
            viewModelScope.launch {
                connectionCandidate.connect(myDeviceName ?: "unknown") { authenticationDigits ->
                    _connectionState.value = ConnectionState.WaitingForAuthentication(
                        connectionCandidate.id,
                        authenticationDigits
                    )
                    suspendCancellableCoroutine { continuation ->
                        waitingForAuthAcceptingContinuations[connectionCandidate.id] = continuation
                    }
                }.onSuccess {
                    ConnectionsManager.addAndStartListeningForMessages(it)
                    _connectionState.value = ConnectionState.Connected(connectionCandidate.id)
                }.onFailure {
                    _connectionState.value =
                        ConnectionState.FailureToConnect(connectionCandidate.id)
                }
            }
        }
    }

    fun disconnect(deviceId: String) {
        ConnectionsManager.disconnect(deviceId)
    }

    fun getNumberOfUnreadMessages(deviceId: String): Int {
        return ConnectionsManager.getMessagesFor(deviceId).filter { it.isProcessed.not() }.size
    }

    fun rejectAuthentication(id: String) {
        Log.d(TAG, "user rejected authentication for $id")
        waitingForAuthAcceptingContinuations.remove(id)?.resume(false)
            ?: error("No waiting for $id")
        _connectionState.value = ConnectionState.RejectedAuthentication(id)
    }

    fun acceptAuthentication(id: String) {
        Log.d(TAG, "user accepted authentication for $id")
        waitingForAuthAcceptingContinuations.remove(id)?.resume(true) ?: error("No waiting for $id")
        _connectionState.value = ConnectionState.AcceptedAuthentication(id)
    }

    private fun listenForConnectionCandidateEvents(flow: Flow<ConnectionCandidateEvent>): Job {
        return viewModelScope.launch {
            flow.collect { event ->
                Log.d(TAG, "Received event: $event")
                when (event.type) {
                    DISCOVERED -> {
                        connectionCandidates.add(event.candidate)
                        _operationState.value = OperationState.Discovered(event.candidate)
                    }

                    LOST -> {
                        //remove waiting accept
                        if (connectionCandidates.remove(event.candidate)) {
                            _operationState.value = OperationState.Lost(event.candidate)
                        }
                    }
                }
            }
        }
    }
}

sealed class OperationState {
    data object InProgress : OperationState()
    data object Success : OperationState()

    data class Failed(val error: String) : OperationState()
    data class Discovered(val connectionCandidate: ConnectionCandidate) : OperationState()
    data class Lost(val connectionCandidate: ConnectionCandidate) : OperationState()
}

sealed class ConnectionState {
    data class InProgress(val id: String) : ConnectionState()

    data class WaitingForAuthentication(val id: String, val authDigits: String) : ConnectionState()

    data class AcceptedAuthentication(val id: String) : ConnectionState()

    data class RejectedAuthentication(val id: String) : ConnectionState()

    data class Connected(val id: String) : ConnectionState()

    data class FailureToConnect(val id: String) : ConnectionState()

    data class Disconnected(val connection: Connection) : ConnectionState()

    data class ReceivedMessage(val id: String, val message: Message) : ConnectionState()
}