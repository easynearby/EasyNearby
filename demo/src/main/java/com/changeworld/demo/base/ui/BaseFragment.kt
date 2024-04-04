package com.changeworld.demo.base.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.changeworld.demo.base.Message
import com.changeworld.demo.base.adapter.DeviceAdapter
import com.changeworld.demo.base.adapter.DeviceViewData
import com.changeworld.demo.base.dialogs.InfoDialog
import com.changeworld.demo.base.viewmodel.BaseViewModel
import com.changeworld.demo.base.viewmodel.ConnectionState
import com.changeworld.demo.base.viewmodel.OperationState
import com.changeworld.demo.databinding.FragmentBaseBinding
import com.changeworld.demo.ui.message.MessageActivity
import kotlinx.coroutines.launch


abstract class BaseFragment : Fragment() {
    private var _binding: FragmentBaseBinding? = null
    val binding get() = _binding!!

    private lateinit var adapter: DeviceAdapter

    abstract fun provideViewModel(): BaseViewModel

    private val viewModel by lazy {
        provideViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeRecyclerView()

        listenSwitcherEvents()

        subscribeToErrorEvents()

        subscribeToOperationEvents()

        subscribeToConnectionEvents()
    }

    private fun subscribeToConnectionEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.connectionState.collect { state ->
                when (state) {
                    is ConnectionState.InProgress -> {
                        adapter.devices.find { it.id == state.id }?.let { deviceViewData ->
                            adapter.changeDevice(
                                deviceViewData.copy(
                                    showProgress = true, authenticationDigits = null
                                )
                            )
                        }
                    }

                    is ConnectionState.WaitingForAuthentication -> {
                        adapter.devices.find { it.id == state.id }?.let { deviceViewData ->
                            adapter.changeDevice(deviceViewData.copy(authenticationDigits = state.authDigits))
                        }
                    }

                    is ConnectionState.AcceptedAuthentication -> {
                        adapter.devices.find { it.id == state.id }?.let { deviceViewData ->
                            adapter.changeDevice(
                                deviceViewData.copy(
                                    authenticationDigits = null,
                                    showProgress = true
                                )
                            )
                        }
                    }

                    is ConnectionState.RejectedAuthentication -> {
                        adapter.devices.find { it.id == state.id }?.let { deviceViewData ->
                            adapter.changeDevice(
                                deviceViewData.copy(
                                    authenticationDigits = null,
                                    showProgress = false
                                )
                            )
                        }
                    }

                    is ConnectionState.Connected -> {
                        adapter.devices.find { it.id == state.id }?.let { deviceViewData ->
                            adapter.changeDevice(
                                deviceViewData.copy(
                                    isConnected = true,
                                    showProgress = false,
                                    authenticationDigits = null
                                )
                            )
                        }
                    }

                    is ConnectionState.FailureToConnect -> {
                        adapter.devices.find { it.id == state.id }?.let { deviceViewData ->
                            adapter.changeDevice(
                                deviceViewData.copy(
                                    showProgress = false, authenticationDigits = null
                                )
                            )
                        }
                    }

                    is ConnectionState.Disconnected -> {
                        adapter.devices.find { it.id == state.connection.id }?.let { deviceViewData ->
                            adapter.changeDevice(
                                deviceViewData.copy(
                                    isConnected = false,
                                    authenticationDigits = null,
                                    showProgress = false,
                                    numberOfReceivedMessages = 0
                                )
                            )
                        }
                    }

                    is ConnectionState.ReceivedMessage -> {
                        val unreadMessagesSize = viewModel.getNumberOfUnreadMessages(state.id)
                        if (unreadMessagesSize > 0) {
                            adapter.devices.find { it.id == state.id }?.let { deviceViewData ->
                                adapter.changeDevice(
                                    deviceViewData.copy(
                                        authenticationDigits = null,
                                        isConnected = true,
                                        showProgress = false,
                                        numberOfReceivedMessages = unreadMessagesSize
                                    )
                                )
                            }
                        }
                    }

                    null -> {
                        // NOP
                    }
                }
            }
        }
    }

    private fun subscribeToOperationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.operationState.collect { state ->
                when (state) {
                    OperationState.InProgress -> {
                        showProgress()
                    }

                    OperationState.Success -> {
                        hideProgress()
                    }

                    is OperationState.Failed -> {
                        binding.operationSwitch.isChecked = false
                    }

                    is OperationState.Discovered -> {
                        adapter.addDevice(
                            DeviceViewData(
                                state.connectionCandidate.id,
                                state.connectionCandidate.name,
                                authenticationDigits = null,
                                isConnected = false,
                                showProgress = false,
                                numberOfReceivedMessages = 0
                            )
                        )
                    }

                    is OperationState.Lost -> {
                        adapter.removeDevice(
                            DeviceViewData(
                                state.connectionCandidate.id,
                                state.connectionCandidate.name,
                                authenticationDigits = null,
                                isConnected = false,
                                showProgress = false,
                                numberOfReceivedMessages = 0
                            )
                        )
                    }

                    null -> {
                        // NOP
                    }
                }
            }
        }
    }

    private fun subscribeToErrorEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorEvents.collect {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun listenSwitcherEvents() {
        binding.operationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showOperationInfoDialog().setListener { deviceInfo ->
                    if (deviceInfo == null) {
                        binding.operationSwitch.isChecked = false
                        return@setListener
                    }
                    viewModel.start(deviceInfo)
                }
            } else {
                viewModel.stop()
            }
        }
    }


    private fun initializeRecyclerView() {
        adapter = DeviceAdapter(listOf(), onConnectionEvent = {
            if (it.isConnected) {
                viewModel.disconnect(it.id)
            } else {
                viewModel.connect(it.id)
            }
        }, onMessageClicked = { deviceViewData ->
            val id = deviceViewData.id
            startActivity(Intent(requireContext(), MessageActivity::class.java).apply {
                putExtra(MessageActivity.EXTRA_DEVICE_ID, id)
            })
            adapter.changeDevice(deviceViewData.copy(numberOfReceivedMessages = 0))
        }, onAuthResult = { deviceViewData, accepted ->
            if (accepted) {
                viewModel.acceptAuthentication(deviceViewData.id)
            } else {
                viewModel.rejectAuthentication(deviceViewData.id)
            }
        })

        binding.listOfDevicesRv.layoutManager = LinearLayoutManager(context)
        binding.listOfDevicesRv.adapter = adapter
    }

    private fun showOperationInfoDialog(): InfoDialog {
        val dialog = InfoDialog()
        dialog.show(childFragmentManager, "AdvertiseInfoDialog")
        return dialog
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
