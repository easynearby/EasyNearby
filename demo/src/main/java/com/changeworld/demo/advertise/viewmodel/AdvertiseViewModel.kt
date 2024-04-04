package com.changeworld.demo.advertise.viewmodel

import com.changeworld.easynearby.EasyNearby
import com.changeworld.easynearby.advertising.DeviceInfo
import com.changeworld.easynearby.connection.ConnectionCandidateEvent
import com.changeworld.demo.base.viewmodel.BaseViewModel
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