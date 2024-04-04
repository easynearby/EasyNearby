package com.changeworld.demo.discover.viewmodel

import com.changeworld.easynearby.EasyNearby
import com.changeworld.easynearby.advertising.DeviceInfo
import com.changeworld.easynearby.connection.ConnectionCandidateEvent
import com.changeworld.demo.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.Flow

class DiscoverViewModel : BaseViewModel() {

    private val discoverManager = EasyNearby.getDiscoverManager()
    override suspend fun startOperation(deviceInfo: DeviceInfo): Result<Flow<ConnectionCandidateEvent>> {
        return discoverManager.startDiscovery(deviceInfo)
    }

    override suspend fun stopOperation() {
        discoverManager.stopDiscovery()
    }
}