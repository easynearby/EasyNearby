package io.github.easynearby.core.connection

import io.github.easynearby.core.advertising.testimpl.testKoinModule
import io.github.easynearby.core.di.IsolatedKoinContext
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.dsl.module

class ConnectionCandidateEventTest {

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
    fun `test Two same devices are equal`() {
        assertThat(
            ConnectionCandidateEvent(
                ConnectionEventType.DISCOVERED, ConnectionCandidate("id", "name", null)
            ), equalTo(
                ConnectionCandidateEvent(
                    ConnectionEventType.DISCOVERED, ConnectionCandidate("id", "name", null)
                )
            )
        )
    }
}