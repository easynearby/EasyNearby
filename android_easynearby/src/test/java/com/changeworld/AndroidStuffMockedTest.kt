package com.changeworld

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.AfterClass
import org.junit.BeforeClass

abstract class AndroidStuffMockedTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            mockkStatic(Log::class)
            every {
                Log.d(any(), any())
            } answers {
                println("${firstArg<String>()}: ${secondArg<String>()}")
                1
            }

            every {
                Log.e(any(), any(), any())
            } answers {
                println("${firstArg<String>()}: ${secondArg<String>()}")
                thirdArg<Throwable?>()?.printStackTrace()
                1
            }

            every { Log.w(any(), any(), any()) } answers {
                println("${firstArg<String>()}: ${secondArg<String>()}")
                thirdArg<Throwable?>()?.printStackTrace()
                1
            }
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            unmockkStatic(Log::class)
        }
    }
}