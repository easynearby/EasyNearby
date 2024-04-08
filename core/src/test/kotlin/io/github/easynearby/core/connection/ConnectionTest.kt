package io.github.easynearby.core.connection

import io.github.easynearby.core.advertising.testimpl.testKoinModule
import io.github.easynearby.core.di.IsolatedKoinContext
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.dsl.module

class ConnectionTest {
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
    fun `test sendPayload When invoked Then pass call to connectionManager`() = runTest {
        val connection = DirectConnection("id", "name", mockk(relaxed = true))
        coEvery { connectionManager.sendPayload("id", any()) } returns Result.success(Unit)

        val result = connection.sendPayload("payload".toByteArray())

        assertThat(result.isSuccess, equalTo(true))
        coVerify(exactly = 1) { connectionManager.sendPayload("id", "payload".toByteArray()) }
    }

    @Test
    fun `test close When invoked Then pass call to connectionManager`() = runTest {
        val connection = DirectConnection("id", "name", mockk(relaxed = true))
        coEvery { connectionManager.disconnect("id") } returns Unit

        connection.close()

        coVerify(exactly = 1) { connectionManager.disconnect("id") }
    }

    @Test
    fun `test equals When use same Object Then return true`() = runTest {
        val connection = DirectConnection("id", "name", mockk(relaxed = true))
        assertThat(connection, equalTo(connection))
    }

    @Test
    fun `test equals When use connections With the same id Then return true`() = runTest {
        val firstConnection = DirectConnection("id", "name", mockk(relaxed = true))
        val secondConnection = DirectConnection("id", "name", mockk(relaxed = true))
        assertThat(firstConnection, equalTo(secondConnection))
    }

    @Test
    fun `test equals When use different Object Then return false`() = runTest {
        val connection1 = DirectConnection("id1", "name", mockk(relaxed = true))
        val connection2 = DirectConnection("id2", "name", mockk(relaxed = true))
        assertThat(connection1, not(equalTo(connection2)))
    }

    @Test
    fun `test hashCode When use same Object Then return same hash`() = runTest {
        val connection = DirectConnection("id", "name", mockk(relaxed = true))
        repeat(10) {
            assertThat(connection.hashCode(), equalTo(connection.hashCode()))
        }
    }
}