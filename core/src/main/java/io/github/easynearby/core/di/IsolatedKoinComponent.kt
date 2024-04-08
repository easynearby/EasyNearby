package io.github.easynearby.core.di

import org.koin.core.Koin
import org.koin.core.component.KoinComponent

internal abstract class IsolatedKoinComponent : KoinComponent {
    override fun getKoin(): Koin = IsolatedKoinContext.koin
}