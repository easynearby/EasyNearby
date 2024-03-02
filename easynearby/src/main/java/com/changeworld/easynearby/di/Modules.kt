package com.changeworld.easynearby.di

import com.changeworld.easynearby.advertising.AdvertiseManager
import com.changeworld.easynearby.connection.ConnectionManager
import com.changeworld.easynearby.discovery.DiscoveryManager
import org.koin.dsl.module

val baseModule = module {
    single { AdvertiseManager(get(), get()) }
    single { ConnectionManager(get()) }
    single { DiscoveryManager(get(), get()) }
}