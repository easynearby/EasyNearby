package com.changeworld.offternetpoc

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.changeworld.easynearby.EasyNearby
import com.changeworld.easynearby.advertising.DeviceInfo
import com.changeworld.easynearby.connection.ConnectionCandidateEvent
import kotlinx.coroutines.flow.Flow

class DiscoverFragment : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.advertisingSwitch.text = "Discover"
        binding.listOfDevicesRv.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.teal_200))

    }

    override suspend fun advertiseOrDiscover(deviceInfo: DeviceInfo): Result<Flow<ConnectionCandidateEvent>> {
        return EasyNearby.getDiscoverManager().startDiscovery(deviceInfo)
    }

    override suspend fun stopAdvertiseOrDiscover() {
        EasyNearby.getDiscoverManager().stopDiscovery()
    }

    override fun isAdvertising(): Boolean {
        return false
    }
}