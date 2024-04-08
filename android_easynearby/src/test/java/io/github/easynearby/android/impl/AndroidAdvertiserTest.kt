package io.github.easynearby.android.impl

import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import io.github.easynearby.android.AndroidStuffMockedTest
import io.github.easynearby.android.utils.FakeTask
import io.github.easynearby.core.ConnectionStrategy
import io.github.easynearby.core.advertising.DeviceInfo
import io.github.easynearby.core.connection.ConnectionCandidate
import io.github.easynearby.core.connection.ConnectionCandidateEvent
import io.github.easynearby.core.connection.ConnectionEventType
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidAdvertiserTest : AndroidStuffMockedTest() {

    private lateinit var connectionsClient: ConnectionsClient
    private lateinit var connectionLifecycleCallback: ConnectionLifecycleCallback
    private lateinit var connectionsEventsFlow: MutableSharedFlow<ConnectionEvents>

    @Before
    fun setUp() {
        connectionsClient = mockk()
        connectionLifecycleCallback = mockk()
        connectionsEventsFlow = MutableSharedFlow()
        every {
            connectionsClient.stopAdvertising()
        } just Runs
    }

    @Test
    fun `test stopAdvertising When invoked then invoke connectionsClient stopAdvertising`() =
        runTest {
            val advertiser = AndroidAdvertiser(
                TestScope(), connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )

            advertiser.stopAdvertising()

            coVerify(exactly = 1) {
                connectionsClient.stopAdvertising()
            }
        }

    @Test
    fun `test startAdvertising When call failed then return exception`() = runTest {
        val testScope = TestScope()
        val advertiser = AndroidAdvertiser(
            testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
        )

        val fakeTask = FakeTask<Void>()
        every {
            connectionsClient.startAdvertising(any<String>(), any(), any(), any())
        } returns fakeTask

        val expectedException = Exception()

        var gotResult = false

        val job = launch {
            val result =
                advertiser.startAdvertising(DeviceInfo("name", "id", ConnectionStrategy.STAR))
            gotResult = true
            assertThat(result.isFailure, equalTo(true))
            assertThat(result.exceptionOrNull(), equalTo(expectedException))
        }

        // wait until idle
        advanceUntilIdle()

        fakeTask.invokeFailureListener(expectedException)
        job.join()

        assertThat(gotResult, equalTo(true))
    }

    @Test
    fun `test startAdvertising When call success then return flow`() = runTest {
        val advertiser = AndroidAdvertiser(
            TestScope(), connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
        )

        val fakeTask = FakeTask<Void?>()

        every {
            connectionsClient.startAdvertising(any<String>(), any(), any(), any())
        } returns fakeTask

        var gotResult = false
        val job = launch {
            val result =
                advertiser.startAdvertising(DeviceInfo("name", "id", ConnectionStrategy.STAR))
            gotResult = true
            assertThat(result.isSuccess, equalTo(true))
        }

        // wait until idle
        advanceUntilIdle()
        fakeTask.invokeSuccessListener(null)
        job.join()

        assertThat(gotResult, equalTo(true))
    }


    @Test
    fun `test startAdvertising When connection initiated with incoming connection Then emit discovered connection candidate `() =
        runTest {
            val testScope = TestScope()

            val advertiser = AndroidAdvertiser(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )

            // subscribe to connections events flow
            testScope.advanceUntilIdle()

            val fakeTask = FakeTask<Void?>()

            every {
                connectionsClient.startAdvertising(any<String>(), any(), any(), any())
            } returns fakeTask

            val result = async {
                advertiser.startAdvertising(DeviceInfo("name", "id", ConnectionStrategy.STAR))
            }

            // run async job
            advanceUntilIdle()

            fakeTask.invokeSuccessListener(null)

            val flow = result.await().getOrThrow()

            val resultList = mutableListOf<ConnectionCandidateEvent>()
            val collectJob = launch(UnconfinedTestDispatcher()) {
                flow.toList(resultList)
            }

            val connectionInfo = ConnectionInfo("endpointName", "token", true)


            // send connection initiated event
            launch {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionInitiated(
                        "endpoint", connectionInfo
                    )
                )
            }

            // run emit flow
            advanceUntilIdle()

            // wait for emitting connection candidate from advertisser
            testScope.advanceUntilIdle()
            assertThat(resultList.size, equalTo(1))
            assertThat(resultList.first().type, equalTo(ConnectionEventType.DISCOVERED))
            assertThat(
                resultList.first().candidate,
                equalTo(ConnectionCandidate("endpoint", "endpointName", "6031"))
            )
            collectJob.cancel()
        }

    @Test
    fun `test startAdvertising When connection initiated with outgoing connection Then don't emit discovered connection candidate `() =
        runTest {
            val testScope = TestScope()

            val advertiser = AndroidAdvertiser(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )

            // subscribe to connections events flow
            testScope.advanceUntilIdle()

            val fakeTask = FakeTask<Void?>()

            every {
                connectionsClient.startAdvertising(any<String>(), any(), any(), any())
            } returns fakeTask

            val result = async {
                advertiser.startAdvertising(DeviceInfo("name", "id", ConnectionStrategy.STAR))
            }

            // run async job
            advanceUntilIdle()

            fakeTask.invokeSuccessListener(null)

            val flow = result.await().getOrThrow()

            val resultList = mutableListOf<ConnectionCandidateEvent>()
            val collectJob = launch(UnconfinedTestDispatcher()) {
                flow.toList(resultList)
            }

            val connectionInfo = ConnectionInfo("endpointName", "token", false)


            // send connection initiated event
            launch {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionInitiated(
                        "endpoint", connectionInfo
                    )
                )
            }

            // run emit flow
            advanceUntilIdle()

            // wait for emitting connection candidate from advertisser
            testScope.advanceUntilIdle()
            assertThat(resultList.size, equalTo(0))
            collectJob.cancel()
        }

    @Test
    fun `test startAdvertising When connection initiated with incoming connection and then ConnectionResult with not success emitted Then emit discovered and then Lost connection candidate `() =
        runTest {
            val testScope = TestScope()

            val advertiser = AndroidAdvertiser(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )

            // subscribe to connections events flow
            testScope.advanceUntilIdle()

            val fakeTask = FakeTask<Void?>()

            every {
                connectionsClient.startAdvertising(any<String>(), any(), any(), any())
            } returns fakeTask

            val result = async {
                advertiser.startAdvertising(DeviceInfo("name", "id", ConnectionStrategy.STAR))
            }

            // run async job
            advanceUntilIdle()

            fakeTask.invokeSuccessListener(null)

            val flow = result.await().getOrThrow()

            val resultList = mutableListOf<ConnectionCandidateEvent>()
            val collectJob = launch(UnconfinedTestDispatcher()) {
                flow.toList(resultList)
            }

            val connectionInfo = ConnectionInfo("endpointName", "token", true)


            // send connection initiated event
            launch {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionInitiated(
                        "endpoint", connectionInfo
                    )
                )
            }

            launch {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionResult(
                        "endpoint", ConnectionResolution(Status.RESULT_DEAD_CLIENT)
                    )
                )
            }

            // run emit flow
            advanceUntilIdle()

            // wait for emitting connection candidate from advertisser
            testScope.advanceUntilIdle()
            assertThat(resultList.size, equalTo(2))
            assertThat(resultList[1].type, equalTo(ConnectionEventType.LOST))
            assertThat(
                resultList[1].candidate,
                equalTo(ConnectionCandidate("endpoint", "endpointName", "6031"))
            )
            collectJob.cancel()
        }

    @Test
    fun `test startAdvertising When Disconnected event emitted Then do nothing `() = runTest {
        val testScope = TestScope()

        val advertiser = AndroidAdvertiser(
            testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
        )

        // subscribe to connections events flow
        testScope.advanceUntilIdle()

        val fakeTask = FakeTask<Void?>()

        every {
            connectionsClient.startAdvertising(any<String>(), any(), any(), any())
        } returns fakeTask

        val result = async {
            advertiser.startAdvertising(DeviceInfo("name", "id", ConnectionStrategy.STAR))
        }

        // run async job
        advanceUntilIdle()

        fakeTask.invokeSuccessListener(null)

        val flow = result.await().getOrThrow()

        val resultList = mutableListOf<ConnectionCandidateEvent>()
        val collectJob = launch(UnconfinedTestDispatcher()) {
            flow.toList(resultList)
        }


        // send connection initiated event
        launch {
            connectionsEventsFlow.emit(
                ConnectionEvents.Disconnected("endpoint")
            )
        }

        // run emit flow
        advanceUntilIdle()

        // wait for emitting connection candidate from advertisser
        testScope.advanceUntilIdle()
        assertThat(resultList.size, equalTo(0))
        collectJob.cancel()
    }

    @Test
    fun `test startAdvertising When same connection initiated more that once with incoming connection Then emit discovered connection candidate only once`() =
        runTest {
            val testScope = TestScope()

            val advertiser = AndroidAdvertiser(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )

            // subscribe to connections events flow
            testScope.advanceUntilIdle()

            val fakeTask = FakeTask<Void?>()

            every {
                connectionsClient.startAdvertising(any<String>(), any(), any(), any())
            } returns fakeTask

            val result = async {
                advertiser.startAdvertising(DeviceInfo("name", "id", ConnectionStrategy.STAR))
            }

            // run async job
            advanceUntilIdle()

            fakeTask.invokeSuccessListener(null)

            val flow = result.await().getOrThrow()

            val resultList = mutableListOf<ConnectionCandidateEvent>()
            val collectJob = launch(UnconfinedTestDispatcher()) {
                flow.toList(resultList)
            }

            val connectionInfo = ConnectionInfo("endpointName", "token", true)


            // send connection initiated event
            launch {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionInitiated(
                        "endpoint", connectionInfo
                    )
                )
            }

            launch {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionInitiated(
                        "endpoint", connectionInfo
                    )
                )
            }

            // run emit flow
            advanceUntilIdle()

            // wait for emitting connection candidate from advertisser
            testScope.advanceUntilIdle()
            assertThat(resultList.size, equalTo(1))
            assertThat(resultList.first().type, equalTo(ConnectionEventType.DISCOVERED))
            assertThat(
                resultList.first().candidate,
                equalTo(ConnectionCandidate("endpoint", "endpointName", "6031"))
            )
            collectJob.cancel()
        }


    @Test
    fun `test startAdvertising When connected and then disconnected Then emit discovered and lost `() =
        runTest {
            val testScope = TestScope()

            val advertiser = AndroidAdvertiser(
                testScope, connectionsClient, connectionLifecycleCallback, connectionsEventsFlow
            )

            // subscribe to connections events flow
            testScope.advanceUntilIdle()

            val fakeTask = FakeTask<Void?>()

            every {
                connectionsClient.startAdvertising(any<String>(), any(), any(), any())
            } returns fakeTask

            val result = async {
                advertiser.startAdvertising(DeviceInfo("name", "id", ConnectionStrategy.STAR))
            }

            // run async job
            advanceUntilIdle()

            fakeTask.invokeSuccessListener(null)

            val flow = result.await().getOrThrow()

            val resultList = mutableListOf<ConnectionCandidateEvent>()
            val collectJob = launch(UnconfinedTestDispatcher()) {
                flow.toList(resultList)
            }


            launch {
                connectionsEventsFlow.emit(
                    ConnectionEvents.ConnectionInitiated(
                        "endpoint",
                        ConnectionInfo("endpointName", "token", true)
                    )
                )
            }
            // send connection initiated event
            launch {
                connectionsEventsFlow.emit(
                    ConnectionEvents.Disconnected("endpoint")
                )
            }

            // run emit flow
            advanceUntilIdle()

            val expectedConnectionCandidate =
                ConnectionCandidate("endpoint", "endpointName", "6031")

            // wait for emitting connection candidate from advertisser
            testScope.advanceUntilIdle()
            assertThat(resultList.size, equalTo(2))
            assertThat(resultList[0].type, equalTo(ConnectionEventType.DISCOVERED))
            assertThat(resultList[0].candidate, equalTo(expectedConnectionCandidate))
            assertThat(resultList[1].type, equalTo(ConnectionEventType.LOST))
            assertThat(resultList[1].candidate, equalTo(expectedConnectionCandidate))
            collectJob.cancel()
        }


}