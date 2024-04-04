package com.changeworld.easynearby.connection

import com.changeworld.easynearby.advertising.testimpl.testKoinModule
import com.changeworld.easynearby.di.IsolatedKoinContext
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module

class ConnectionManagerTest {
    companion object {

        private var connector: Connector = mockk(relaxed = true)

        @BeforeAll
        @JvmStatic
        fun beforeClass() {
            IsolatedKoinContext.koin.loadModules(listOf(testKoinModule))
        }

        @AfterAll
        @JvmStatic
        fun afterClass() {
            IsolatedKoinContext.koin.unloadModules(listOf(testKoinModule))
        }
    }

    private lateinit var connectionManager: ConnectionManager

    @BeforeEach
    fun setUp() {
        connectionManager = ConnectionManager(connector)
    }

    @Test
    fun `test connect When invoked Then pass call to connector`() = runTest {
        val id = "id"
        val modules = listOf(module { single { connectionManager } })
        IsolatedKoinContext.koin.loadModules(modules)

        val connection = DirectConnection(id, "name", Channel<ByteArray>().receiveAsFlow())
        coEvery { connector.connect(id, any(), any(), any(),any()) } returns Result.success(connection)

        val result = connectionManager.connect(id, "name", "remoteName", false, { true })

        assertThat(result.isSuccess, equalTo(true))
        assertThat(result.getOrThrow().id, equalTo(connection.id))
        coVerify(exactly = 1) { connector.connect(id, any(), any(),any(), any()) }

        IsolatedKoinContext.koin.unloadModules(modules)
    }

    @Test
    fun `test disconnect When invoked Then pass call to connector`() = runTest {
        val id = "id"
        coEvery { connector.disconnect(id) } returns Unit

        connectionManager.disconnect(id)

        coVerify(exactly = 1) { connector.disconnect(id) }
    }

    @Test
    fun `test sendPayload When invoked Then pass call to connector`() = runTest {
        val id = "id"
        val payload = "payload"
        coEvery { connector.sendPayload(id, payload.toByteArray()) } returns Result.success(Unit)

        val result = connectionManager.sendPayload(id, payload.toByteArray())

        assertThat(result.isSuccess, equalTo(true))
        coVerify(exactly = 1) { connector.sendPayload(id, payload.toByteArray()) }
    }
}