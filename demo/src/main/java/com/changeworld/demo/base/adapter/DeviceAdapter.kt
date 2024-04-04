package com.changeworld.demo.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.changeworld.demo.R
import com.changeworld.demo.databinding.ItemDeviceBinding

class DeviceAdapter(
    deviceViewData: List<DeviceViewData>,
    private val onConnectionEvent: (DeviceViewData) -> Unit,
    private val onMessageClicked: (DeviceViewData) -> Unit,
    private val onAuthResult: (deviceViewData: DeviceViewData, accepted: Boolean) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviViewHolder>() {
    private val _devices = deviceViewData.toMutableList()
    val devices: List<DeviceViewData>
        get() = _devices


    class DeviViewHolder(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviViewHolder, position: Int) {
        val device = _devices[position]
        with(holder) {
            binding.progressLabel.isVisible = device.showProgress
            device.authenticationDigits?.let { digits ->
                binding.digitsLabel.text = "Authentication digits: $digits"
                binding.acceptAuthImage.setOnClickListener {
                    onAuthResult(device, true)
                }
                binding.declineAuthImage.setOnClickListener {
                    onAuthResult(device, false)
                }
                binding.digitsLayout.visibility = View.VISIBLE
            } ?: run {
                binding.digitsLayout.visibility = View.INVISIBLE
            }
            binding.endpointLabel.text = device.name

            if (device.isConnected) {
                binding.messageBadge.visibility = View.VISIBLE
            }else{
                binding.messageBadge.visibility = View.GONE
            }
            binding.messageBadge.badgeValue = device.numberOfReceivedMessages
            val connectDisconnectImage = when {
                device.isConnected -> R.drawable.disconnect
                else -> R.drawable.connect
            }

            binding.messageBadge.setOnClickListener {
                onMessageClicked(device)
            }

            binding.messageBadge.isEnabled = device.isConnected

            binding.connectDisconnectImg.setImageResource(connectDisconnectImage)

            binding.connectDisconnectImg.setOnClickListener {
                onConnectionEvent(device)
            }
        }
    }

    override fun getItemCount(): Int {
        return _devices.size
    }

    fun addDevice(deviceViewData: DeviceViewData) {
        _devices.add(deviceViewData)
        notifyItemInserted(_devices.lastIndex + 1)
    }

    fun removeDevice(deviceViewData: DeviceViewData) {
        _devices.indexOf(deviceViewData).takeIf { it != -1 }?.let { position ->
            _devices.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun changeDevice(deviceViewData: DeviceViewData) {
        _devices.indexOf(deviceViewData).takeIf { it != -1 }?.let { position ->
            _devices.removeAt(position)
            _devices.add(position, deviceViewData)
            notifyItemChanged(position)
        }
    }
}

data class DeviceViewData(
    val id: String,
    val name: String,
    val authenticationDigits: String?,
    val isConnected: Boolean,
    val showProgress: Boolean,
    val numberOfReceivedMessages: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceViewData

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}