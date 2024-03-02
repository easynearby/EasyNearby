package com.changeworld.easynearby.advertising.testimpl

import com.changeworld.easynearby.loggging.Logger
import org.koin.dsl.module

val testKoinModule = module {
    single<Logger> { ConsoleLoggerImpl() }
}