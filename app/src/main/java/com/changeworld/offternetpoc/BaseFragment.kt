package com.changeworld.offternetpoc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.changeworld.easynearby.advertising.DeviceInfo
import com.changeworld.easynearby.connection.Connection
import com.changeworld.easynearby.connection.ConnectionCandidate
import com.changeworld.easynearby.connection.ConnectionCandidateEvent
import com.changeworld.easynearby.connection.ConnectionEventType
import com.changeworld.offternetpoc.databinding.FragmentBaseBinding
import com.changeworld.offternetpoc.dialogs.InfoDialog
import com.changeworld.offternetpoc.dialogs.MessagesDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine


abstract class BaseFragment : Fragment() {
    private var _binding: FragmentBaseBinding? = null
    val binding get() = _binding!!


    private val connectionCandidates = mutableListOf<ConnectionCandidate>()
    private val connections = mutableListOf<Connection>()
    private val communicationMessages = mutableMapOf<String, MutableList<Message>>()

    private lateinit var adapter: DeviceAdapter
    private lateinit var messageDialog: MessagesDialog
    private var advertiseJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messageDialog = MessagesDialog()
        createAdapterWithListeners()
        binding.listOfDevicesRv.layoutManager = LinearLayoutManager(context)
        binding.listOfDevicesRv.adapter = adapter
        binding.advertisingSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showAdvertiseInfoDialog().setListener {
                    listenForConnectionCandidates(it)
                }
            } else {
                stopAdvertising()
            }
        }
    }


    private fun createAdapterWithListeners() {
        adapter = DeviceAdapter(
            listOf(),
            onConnectionEvent = {
                if (it.isConnected) {
                    disconnect(it)
                } else {
                    connect(it)
                }
            }, onMessageClicked = { deviceViewData ->
                val id = deviceViewData.id
                messageDialog.show(childFragmentManager, "MessagesDialog").also {

                    messageDialog.setSendMessageListener { payload ->
                        sendPayload(id, payload)
                    }

                    messageDialog.setMessages(communicationMessages[id] ?: listOf()).also {
                        communicationMessages[id]?.forEach {
                            it.isViewed = true
                        }
                        val newDeviceNewData = deviceViewData.copy(numberOfReceivedMessages = 0)
                        adapter.changeDevice(newDeviceNewData)
                    }
                }
            })
    }

    private fun sendPayload(id: String, payload: String) {
        connections.find { it.id == id }?.let { connection ->
            viewLifecycleOwner.lifecycleScope.launch {
                connection.sendPayload(payload.toByteArray()).onSuccess {
                    val newMessage =
                        Message(connection.id, payload, MessageType.OUTGOING, true)
                    communicationMessages.compute(connection.id) { _, messages ->
                        val newList = (messages ?: mutableListOf())
                        newList.add(newMessage)
                        newList
                    }
                    messageDialog.addMessage(newMessage)
                }
            }
        }
    }

    private fun disconnect(deviceViewData: DeviceViewData) {
        connections.find {
            it.id == deviceViewData.id
        }?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                it.close()
                adapter.changeDevice(deviceViewData.copy(isConnected = false))
            }
        }
    }

    private fun connect(deviceViewData: DeviceViewData) {
        connectionCandidates.find {
            it.id == deviceViewData.id
        }?.let { connectionCandidate ->
            adapter.changeDevice(deviceViewData.copy(showProgress = true))
            viewLifecycleOwner.lifecycleScope.launch {
                connectionCandidate.connect()
                    .onSuccess {
                        listenMessages(it)
                        connectionCandidates.remove(connectionCandidate)
                        connections.add(it)
                        adapter.changeDevice(
                            deviceViewData.copy(
                                isConnected = true,
                                showProgress = false
                            )
                        )
                    }.onFailure {
                        adapter.changeDevice(deviceViewData.copy(showProgress = false))
                    }
            }
        }
    }

    private fun listenMessages(connection: Connection) {
        viewLifecycleOwner.lifecycleScope.launch {
            connection.getPayload().onCompletion {
                if (connections.remove(connection)) {
                    adapter.removeDevice(
                        DeviceViewData(
                            connection.id,
                            connection.id,
                            false,
                            false,
                            0
                        )
                    )
                }
            }.collect { payload ->
                val message =
                    Message(
                        connection.id,
                        String(payload),
                        MessageType.INCOMMING,
                        messageDialog.isVisible
                    )
                val messages =
                    communicationMessages.compute(connection.id) { par, messages ->
                        val newList = (messages ?: mutableListOf())
                        newList.add(message)
                        newList
                    }
                val unreadMessagesSize = messages?.filter { !it.isViewed }?.size ?: 0
                if (unreadMessagesSize > 0) {
                    val deviceViewData =
                        DeviceViewData(
                            connection.id,
                            connection.name,
                            true,
                            false,
                            unreadMessagesSize
                        )
                    adapter.changeDevice(deviceViewData)
                } else {
                    messageDialog.addMessage(message)
                }
            }
        }
    }

    private fun showAdvertiseInfoDialog(): InfoDialog {
        val dialog = InfoDialog()
        dialog.show(childFragmentManager, "AdvertiseInfoDialog")
        return dialog
    }

    abstract suspend fun advertiseOrDiscover(deviceInfo: DeviceInfo): Result<Flow<ConnectionCandidateEvent>>
    abstract suspend fun stopAdvertiseOrDiscover()
    abstract fun isAdvertising(): Boolean

    private fun listenForConnectionCandidates(deviceInfo: DeviceInfo?) {
        if (deviceInfo == null) {
            binding.advertisingSwitch.isChecked = false
            return
        }

        showProgress()
        viewLifecycleOwner.lifecycleScope.launch {
            advertiseOrDiscover(
                DeviceInfo(deviceInfo.name, deviceInfo.serviceId, deviceInfo.strategy)
            ).onSuccess {
                listenForEvents(it)
            }.onFailure {
                it.printStackTrace()
                binding.advertisingSwitch.isChecked = false
                Toast.makeText(
                    requireContext(),
                    "Failed to start advertising",
                    Toast.LENGTH_LONG
                ).show()
            }
            hideProgress()
        }
    }

    private fun stopAdvertising() {
        lifecycleScope.launch {
            stopAdvertiseOrDiscover()
        }
    }


    private fun listenForEvents(connectionCandidateEventsFlow: Flow<ConnectionCandidateEvent>) {
        advertiseJob?.cancel()
        advertiseJob = lifecycleScope.launch {
            connectionCandidateEventsFlow.filter { isAdvertising() == it.candidate.isIncomingConnection }
                .collect { event ->
                    when (event.type) {
                        ConnectionEventType.DISCOVERED -> {
                            connectionCandidates.add(event.candidate)
                            adapter.addDevice(
                                DeviceViewData(
                                    event.candidate.id,
                                    event.candidate.name,
                                    false,
                                    false,
                                    0
                                )
                            )
                        }

                        ConnectionEventType.LOST -> {
                            if (connectionCandidates.remove(event.candidate) || connections.removeIf { it.id == event.candidate.id }) {
                                communicationMessages.remove(event.candidate.id)
                                adapter.removeDevice(
                                    DeviceViewData(
                                        event.candidate.id,
                                        event.candidate.name,
                                        false,
                                        false,
                                        0
                                    )
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun showProgress() {
        binding.progressLabel.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressLabel.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

data class Message(
    val from: String,
    val message: String,
    val type: MessageType,
    var isViewed: Boolean
)

enum class MessageType {
    INCOMMING, OUTGOING
}