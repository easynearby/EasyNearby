package com.changeworld.offternetpoc

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.changeworld.easynearby.EasyNearby
import com.changeworld.easynearby.advertising.DeviceInfo
import com.changeworld.easynearby.connection.ConnectionCandidateEvent
import kotlinx.coroutines.flow.Flow

class AdvertiseFragment: BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.advertisingSwitch.text = "Advertise"
        binding.listOfDevicesRv.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.teal_700))
    }
    override suspend fun advertiseOrDiscover(deviceInfo: DeviceInfo): Result<Flow<ConnectionCandidateEvent>> {
        return EasyNearby.getAdvertiseManager().startAdvertising(deviceInfo)
    }

    override suspend fun stopAdvertiseOrDiscover() {
        EasyNearby.getAdvertiseManager().stopAdvertising()
    }

    override fun isAdvertising(): Boolean {
        return true
    }
}