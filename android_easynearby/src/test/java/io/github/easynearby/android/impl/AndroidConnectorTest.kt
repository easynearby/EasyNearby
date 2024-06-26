package io.github.easynearby.android.impl

import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import io.github.easynearby.android.AndroidStuffMockedTest
import io.github.easynearby.android.utils.FakeTask
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidConnectorTest : AndroidStuffMockedTest() {
    private lateinit var connectionsClient: ConnectionsClient
    private lateinit var connectionLifecycleCallback: ConnectionLifecycleCallback
    private lateinit var connectionsEventsFlow: MutableSharedFlow<ConnectionEvents>


    @Before
    fun setUp() {
        connectionsClient = mockk()
        connectionLifecycleCallback = mockk()
        connectionsEventsFlow = MutableSharedFlow()
    }

    @Test
    fun `test connect When invoked with isIncomingConnection true then invoke connectionsClient accept`() =
        runTest {
            val testScope = TestScope()
            val connector = AndroidConnector(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )
            val fakeTask = FakeTask<Void>()
            every { connectionsClient.acceptConnection("endpoint", any()) } returns fakeTask


            val job = launch {
                connector.connect("endpoint", "name", "remoteName", true, { true })
            }

            advanceUntilIdle()

            job.cancel()

            verify(exactly = 1) {
                connectionsClient.acceptConnection("endpoint", any())
            }
        }

    @Test
    fun `test connect When invoked with isIncomingConnection false then invoke connectToEndpoint accept`() =
        runTest {
            val testScope = TestScope()
            val connector = AndroidConnector(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )
            val fakeTask = FakeTask<Void>()
            every {
                connectionsClient.requestConnection(
                    "name",
                    "endpoint",
                    connectionLifecycleCallback
                )
            } returns fakeTask


            val job = launch {
                connector.connect("endpoint", "name", "remoteName", false, { true })
            }

            advanceUntilIdle()

            job.cancel()

            verify(exactly = 1) {
                connectionsClient.requestConnection("name", "endpoint", connectionLifecycleCallback)
            }
        }

    @Test
    fun `test connect When task successful for incoming connection and connection result is emitted with a success state Then return Connection`() =
        runTest {
            val testScope = TestScope()
            val connector = AndroidConnector(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )
            val fakeTask = FakeTask<Void?>()
            every { connectionsClient.acceptConnection("endpoint", any()) } returns fakeTask


            val result = async {
                connector.connect("endpoint", "name", "remoteName", true, { true })
            }

            testScope.advanceUntilIdle()
            advanceUntilIdle()

            fakeTask.invokeSuccessListener(null)

            launch(UnconfinedTestDispatcher()) {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionResult(
                        "endpoint",
                        ConnectionResolution(Status.RESULT_SUCCESS)
                    )
                )
            }

            testScope.advanceUntilIdle()

            assertThat(result.await().getOrThrow().id, equalTo(("endpoint")))
        }

    @Test
    fun `test connect When connection initiated emited with outgoing connection but pending connection not found Then don't invoke acceptConnection on a client`() =
        runTest {
            val testScope = TestScope()
            val connector = AndroidConnector(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )

            testScope.advanceUntilIdle()

            val fakeTask = FakeTask<Void?>()
            every {
                connectionsClient.acceptConnection("name", any())
            } returns fakeTask

            launch(UnconfinedTestDispatcher()) {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionInitiated(
                        "endpoint",
                        ConnectionInfo("name", "id", false)
                    )
                )
            }

            testScope.advanceUntilIdle()

            verify(inverse = true) {
                connectionsClient.acceptConnection("name", any())
            }
        }

    @Test
    fun `test connect When connection initiated emitted with outgoing connection and pending connection  found Then invoke acceptConnection on a client`() =
        runTest {
            val testScope = TestScope()
            val connector = AndroidConnector(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )

            testScope.advanceUntilIdle()

            val requestEndpointFakeResult = FakeTask<Void?>()
            every {
                connectionsClient.requestConnection("name", "endpoint", any())
            } returns requestEndpointFakeResult

            val acceptConnectionFakeResult = FakeTask<Void?>()
            every {
                connectionsClient.acceptConnection("endpoint", any())
            } returns acceptConnectionFakeResult


            val launchJob = launch(UnconfinedTestDispatcher()) {
                connector.connect("endpoint", "name", "remoteName", false, { true })
            }

            testScope.advanceUntilIdle()
            advanceUntilIdle()

            requestEndpointFakeResult.invokeSuccessListener(null)

            launch(UnconfinedTestDispatcher()) {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionInitiated(
                        "endpoint",
                        ConnectionInfo("name", "id", false)
                    )
                )
            }

            testScope.advanceUntilIdle()

            acceptConnectionFakeResult.invokeSuccessListener(null)

            verify(exactly = 1) {
                connectionsClient.acceptConnection("endpoint", any())
            }

            launchJob.cancel()
        }

    @Test
    fun `test connect When connection initiated emitted with outgoing connection but authentication declined Then don't invoke acceptConnection on a client and return failure`() =
        runTest {
            val testScope = TestScope()
            val connector = AndroidConnector(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )

            testScope.advanceUntilIdle()

            val requestEndpointFakeResult = FakeTask<Void?>()
            val rejectFakeResult = FakeTask<Void?>()
            every {
                connectionsClient.requestConnection("name", "endpoint", any())
            } returns requestEndpointFakeResult

            every { connectionsClient.rejectConnection("endpoint") } returns rejectFakeResult

            val result = async {
                connector.connect("endpoint", "name", "remoteName", false, { false })
            }

            testScope.advanceUntilIdle()
            advanceUntilIdle()

            requestEndpointFakeResult.invokeSuccessListener(null)

            launch(UnconfinedTestDispatcher()) {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionInitiated(
                        "endpoint",
                        ConnectionInfo("name", "id", false)
                    )
                )
            }

            testScope.advanceUntilIdle()
            rejectFakeResult.invokeSuccessListener(null)

            launch(UnconfinedTestDispatcher()) {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionResult(
                        "endpoint",
                        ConnectionResolution(Status.RESULT_CANCELED)
                    )
                )
            }

            testScope.advanceUntilIdle()

            assertThat(result.await().isFailure, equalTo(true))

            verify(inverse = true) {
                connectionsClient.acceptConnection("endpoint", any())
            }
        }

    @Test
    fun `test connect When connection initiated emited with incomming connection Then don't invoke acceptConnection on a client`() =
        runTest {
            val testScope = TestScope()
            val connector = AndroidConnector(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )

            testScope.advanceUntilIdle()

            launch(UnconfinedTestDispatcher()) {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionInitiated(
                        "endpoint",
                        ConnectionInfo("name", "id", true)
                    )
                )
            }

            testScope.advanceUntilIdle()

            verify(inverse = true) {
                connectionsClient.acceptConnection("endpoint", any())
            }
        }

    @Test
    fun `test connect When task successful for outgoing connection and connection result is emitted with a success state Then return Connection`() =
        runTest {
            val testScope = TestScope()
            val connector = AndroidConnector(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )
            val fakeTask = FakeTask<Void?>()
            every {
                connectionsClient.requestConnection(
                    "name",
                    "endpoint",
                    connectionLifecycleCallback
                )
            } returns fakeTask


            val result = async {
                connector.connect("endpoint", "name", "remoteName", false, { true })
            }

            testScope.advanceUntilIdle()
            advanceUntilIdle()

            fakeTask.invokeSuccessListener(null)

            launch(UnconfinedTestDispatcher()) {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionResult(
                        "endpoint",
                        ConnectionResolution(Status.RESULT_SUCCESS)
                    )
                )
            }

            testScope.advanceUntilIdle()

            assertThat(result.await().getOrThrow().id, equalTo("endpoint"))
        }

    @Test
    fun `test connect to incomming connection When task is not successful Then return Failure`() =
        runTest {
            val testScope = TestScope()
            val connector = AndroidConnector(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )
            val fakeTask = FakeTask<Void?>()
            every { connectionsClient.acceptConnection("endpoint", any()) } returns fakeTask


            val result = async {
                connector.connect("endpoint", "name", "remoteName", true, { true })
            }

            testScope.advanceUntilIdle()
            advanceUntilIdle()

            fakeTask.invokeFailureListener(RuntimeException())

            testScope.advanceUntilIdle()

            assertThat(
                result.await().exceptionOrNull(),
                `is`(instanceOf(RuntimeException::class.java))
            )
        }

    @Test
    fun `test connect to outgoing connection When task is not successful Then return Failure`() =
        runTest {
            val testScope = TestScope()
            val connector = AndroidConnector(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )
            val fakeTask = FakeTask<Void?>()
            every {
                connectionsClient.requestConnection(
                    "name",
                    "endpoint",
                    connectionLifecycleCallback
                )
            } returns fakeTask


            val result = async {
                connector.connect("endpoint", "name", "remoteName", false, { true })
            }

            testScope.advanceUntilIdle()
            advanceUntilIdle()

            fakeTask.invokeFailureListener(RuntimeException())

            testScope.advanceUntilIdle()

            assertThat(
                result.await().exceptionOrNull(),
                `is`(instanceOf(RuntimeException::class.java))
            )
        }

    @Test
    fun `test connect When task successful and connection result is emitted with a success state but connect coroutine was cancelled Then don't return`() =
        runTest {
            val testScope = TestScope()
            val connector = AndroidConnector(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )
            val fakeTask = FakeTask<Void?>()
            every { connectionsClient.acceptConnection("endpoint", any()) } returns fakeTask


            val result = async {
                connector.connect("endpoint", "name", "remoteName", true, { true })
            }

            testScope.advanceUntilIdle()
            advanceUntilIdle()

            fakeTask.invokeSuccessListener(null)

            result.cancel()

            launch(UnconfinedTestDispatcher()) {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionResult(
                        "endpoint",
                        ConnectionResolution(Status.RESULT_SUCCESS)
                    )
                )
            }

            testScope.advanceUntilIdle()

            val connectResult = runCatching {
                result.await().getOrThrow()
            }

            assertThat(connectResult.isFailure, equalTo(true))
        }
}