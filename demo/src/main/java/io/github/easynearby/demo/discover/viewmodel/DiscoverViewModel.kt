package io.github.easynearby.demo.discover.viewmodel

import io.github.easynearby.demo.base.viewmodel.BaseViewModel
import io.github.easynearby.core.EasyNearby
import io.github.easynearby.core.advertising.DeviceInfo
import io.github.easynearby.core.connection.ConnectionCandidateEvent
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