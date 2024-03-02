package com.changeworld.offternetpoc

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.changeworld.offternetpoc.databinding.ItemDeviceBinding

class DeviceAdapter(
    deviceViewData: List<DeviceViewData>,
    private val onConnectionEvent: (DeviceViewData) -> Unit,
    private val onMessageClicked: (DeviceViewData) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviViewHolder>() {
    private val devices = deviceViewData.toMutableList()

    class DeviViewHolder(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviViewHolder, position: Int) {
        val device = devices[position]
        with(holder) {
            binding.progressLabel.isVisible = device.showProgress
            binding.endpointLabel.text = device.name
            binding.messageBadge.badgeValue = device.numberOfReceivedMessages
            val connectDisconnectImage = when {
                device.isConnected -> R.drawable.disconnect_24
                else -> R.drawable.round_connect_24
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
        return devices.size
    }

    fun addDevice(deviceViewData: DeviceViewData) {
        devices.add(deviceViewData)
        notifyItemInserted(devices.lastIndex + 1)
    }

    fun removeDevice(deviceViewData: DeviceViewData) {
        devices.indexOf(deviceViewData).takeIf { it != -1 }?.let { position ->
            devices.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun changeDevice(deviceViewData: DeviceViewData) {
        devices.indexOf(deviceViewData).takeIf { it != -1 }?.let { position ->
            devices.removeAt(position)
            devices.add(position, deviceViewData)
            notifyItemChanged(position)
        }
    }
}

data class DeviceViewData(
    val id: String,
    val name:String,
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