package io.github.easynearby.core.di

import io.github.easynearby.core.advertising.AdvertiseManager
import io.github.easynearby.core.connection.ConnectionManager
import io.github.easynearby.core.discovery.DiscoveryManager
import org.koin.dsl.module

val baseModule = module {
    single { AdvertiseManager(get(), get()) }
    single { ConnectionManager(get()) }
    single { DiscoveryManager(get(), get()) }
}