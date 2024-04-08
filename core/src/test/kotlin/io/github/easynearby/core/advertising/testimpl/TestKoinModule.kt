package io.github.easynearby.core.advertising.testimpl

import io.github.easynearby.core.loggging.Logger
import org.koin.dsl.module

val testKoinModule = module {
    single<Logger> { ConsoleLoggerImpl() }
}