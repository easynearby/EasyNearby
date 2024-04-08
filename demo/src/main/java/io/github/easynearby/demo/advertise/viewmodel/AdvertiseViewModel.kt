package io.github.demo.advertise.viewmodel

import io.github.easynearby.demo.base.viewmodel.BaseViewModel
import io.github.easynearby.core.EasyNearby
import io.github.easynearby.core.advertising.DeviceInfo
import io.github.easynearby.core.connection.ConnectionCandidateEvent
import kotlinx.coroutines.flow.Flow

class AdvertiseViewModel : BaseViewModel() {

    private val advertiseManager = EasyNearby.getAdvertiseManager()
    override suspend fun startOperation(deviceInfo: DeviceInfo): Result<Flow<ConnectionCandidateEvent>> {
        return advertiseManager.startAdvertising(deviceInfo)
    }

    override suspend fun stopOperation() {
        advertiseManager.stopAdvertising()
    }
}