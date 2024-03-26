package com.changeworld.easynearby.connection

import com.changeworld.easynearby.advertising.testimpl.testKoinModule
import com.changeworld.easynearby.di.IsolatedKoinContext
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.dsl.module

class ConnectionCandidateTest {
    companion object {

        private var connectionManager: ConnectionManager = mockk(relaxed = true)

        @BeforeAll
        @JvmStatic
        fun beforeClass() {
            IsolatedKoinContext.koin.loadModules(
                listOf(
                    testKoinModule,
                    module { single { connectionManager } })
            )
        }

        @AfterAll
        @JvmStatic
        fun afterClass() {
            IsolatedKoinContext.koin.unloadModules(listOf(testKoinModule))
        }
    }

    @Test
    fun `test connect When invoked Then pass call to connectionManager`() = runTest {
        val connectionCandidate = ConnectionCandidate("id", "name", null)
        coEvery { connectionManager.connect("id", "name", false, { true }) } returns Result.success(
            mockk()
        )

        val result = connectionCandidate.connect({ true })

        assertThat(result.isSuccess, equalTo(true))
        coVerify(exactly = 1) { connectionManager.connect("id", "name", false, any()) }
    }

    @Test
    fun `test connect When invoked and authValidator returns false Then returns failure`() =
        runTest {
            val connectionCandidate = ConnectionCandidate("id", "name", 1234.toString())

            val result = connectionCandidate.connect { false }
            assertThat(result.isFailure, equalTo(true))
        }
}