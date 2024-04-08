package io.github.easynearby.core.di

import org.koin.dsl.koinApplication

internal object IsolatedKoinContext {

    val koinApp = koinApplication {
        modules(baseModule)
    }

    val koin = koinApp.koin
}