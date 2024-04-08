package io.github.easynearby.android.impl

import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
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
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidDiscoverTest : AndroidStuffMockedTest() {

    private lateinit var connectionsClient: ConnectionsClient


    @Before
    fun setUp() {
        connectionsClient = mockk()
    }


    @Test
    fun `test stopAdvertising When invoked then invoke connectionsClient stopAdvertising`() =
        runTest {
            every {
                connectionsClient.stopDiscovery()
            } just Runs

            val discover = AndroidDiscover(TestScope(), connectionsClient)

            discover.stopDiscovery()

            coVerify(exactly = 1) {
                connectionsClient.stopDiscovery()
            }
        }

    @Test
    fun `test startDiscovery When call failed then return exception`() = runTest {
        val fakeTask = FakeTask<Void>()
        every {
            connectionsClient.startDiscovery(any(), any(), any())
        } returns fakeTask

        val testScope = TestScope()
        val discover = AndroidDiscover(testScope, connectionsClient)

        val expectedException = Exception()

        var gotResult = false

        val job = launch {
            val result = discover.startDiscovery(DeviceInfo("name", "id", ConnectionStrategy.STAR))
            gotResult = true
            assertThat(result.isFailure, equalTo(true))
            assertThat(result.exceptionOrNull(), equalTo(expectedException))
        }

        advanceUntilIdle()

        fakeTask.invokeFailureListener(expectedException)
        job.join()

        assertThat(gotResult, equalTo(true))
    }

    @Test
    fun `test startDiscovery When call success then return flow`() = runTest {
        val fakeTask = FakeTask<Void?>()
        every {
            connectionsClient.startDiscovery(any(), any(), any())
        } returns fakeTask

        val testScope = TestScope()
        val discover = AndroidDiscover(testScope, connectionsClient)


        var gotResult = false

        val job = launch {
            val result = discover.startDiscovery(DeviceInfo("name", "id", ConnectionStrategy.STAR))
            gotResult = true
            assertThat(result.isSuccess, equalTo(true))
        }

        advanceUntilIdle()

        fakeTask.invokeSuccessListener(null)
        job.join()

        assertThat(gotResult, equalTo(true))
    }

    @Test
    fun `test startDiscovery When onEndpointFound invoked Then emit discovered connection candidate`() =
        runTest {
            val fakeTask = FakeTask<Void?>()
            var callback: EndpointDiscoveryCallback? = null
            every {
                connectionsClient.startDiscovery(any(), any(), any())
            } answers {
                callback = secondArg()
                fakeTask
            }

            val testScope = TestScope()
            val discover = AndroidDiscover(testScope, connectionsClient)

            val result = async {
                discover.startDiscovery(DeviceInfo("name", "id", ConnectionStrategy.STAR))
            }

            // run async job
            advanceUntilIdle()

            fakeTask.invokeSuccessListener(null)

            val flow = result.await().getOrThrow()

            val resultList = mutableListOf<ConnectionCandidateEvent>()
            val collectJob = launch(UnconfinedTestDispatcher()) {
                flow.toList(resultList)
            }

            callback!!.onEndpointFound(
                "endpointId",
                DiscoveredEndpointInfo("serviceId", "endpointName")
            )

            testScope.advanceUntilIdle()
            assertThat(resultList.size, equalTo(1))
            assertThat(resultList.first().type, equalTo(ConnectionEventType.DISCOVERED))
            assertThat(
                resultList.first().candidate,
                IsEqual.equalTo(ConnectionCandidate("endpointId", "endpointName", null))
            )
            collectJob.cancel()
        }

    @Test
    fun `test startDiscovery When onEndpointLost invoked after it's found Then emit Lost connection candidate`() =
        runTest {
            val fakeTask = FakeTask<Void?>()
            var callback: EndpointDiscoveryCallback? = null
            every {
                connectionsClient.startDiscovery(any(), any(), any())
            } answers {
                callback = secondArg()
                fakeTask
            }

            val testScope = TestScope()
            val discover = AndroidDiscover(testScope, connectionsClient)

            val result = async {
                discover.startDiscovery(DeviceInfo("name", "id", ConnectionStrategy.STAR))
            }

            // run async job
            advanceUntilIdle()

            fakeTask.invokeSuccessListener(null)

            val flow = result.await().getOrThrow()

            val resultList = mutableListOf<ConnectionCandidateEvent>()
            val collectJob = launch(UnconfinedTestDispatcher()) {
                flow.toList(resultList)
            }

            callback!!.onEndpointFound(
                "endpointId",
                DiscoveredEndpointInfo("serviceId", "endpointName")
            )
            testScope.advanceUntilIdle()
            callback!!.onEndpointLost("endpointId")
            testScope.advanceUntilIdle()
            assertThat(resultList.size, equalTo(2))
            assertThat(resultList[1].type, equalTo(ConnectionEventType.LOST))
            assertThat(
                resultList[1].candidate,
                IsEqual.equalTo(ConnectionCandidate("endpointId", "endpointName", null))
            )
            collectJob.cancel()
        }

    @Test
    fun `test startDiscovery When onEndpointLost invoked but not previously found Then emit don't emitLost connection candidate`() =
        runTest {
            val fakeTask = FakeTask<Void?>()
            var callback: EndpointDiscoveryCallback? = null
            every {
                connectionsClient.startDiscovery(any(), any(), any())
            } answers {
                callback = secondArg()
                fakeTask
            }

            val testScope = TestScope()
            val discover = AndroidDiscover(testScope, connectionsClient)

            val result = async {
                discover.startDiscovery(DeviceInfo("name", "id", ConnectionStrategy.STAR))
            }

            // run async job
            advanceUntilIdle()

            fakeTask.invokeSuccessListener(null)

            val flow = result.await().getOrThrow()

            val resultList = mutableListOf<ConnectionCandidateEvent>()
            val collectJob = launch(UnconfinedTestDispatcher()) {
                flow.toList(resultList)
            }

            callback!!.onEndpointLost("endpointId")
            testScope.advanceUntilIdle()

            assertThat(resultList.size, equalTo(0))

            collectJob.cancel()
        }

    @Test
    fun `test startDiscovery When same endpoint found more than once then emit connection candidate only once`() =
        runTest {
            val fakeTask = FakeTask<Void?>()
            val callbackSlot = slot<EndpointDiscoveryCallback>()
            every {
                connectionsClient.startDiscovery(any(), capture(callbackSlot), any())
            } returns fakeTask


            val testScope = TestScope()
            val discover = AndroidDiscover(testScope, connectionsClient)

            val result = async {
                discover.startDiscovery(DeviceInfo("name", "id", ConnectionStrategy.STAR))
            }

            // run async job
            advanceUntilIdle()

            fakeTask.invokeSuccessListener(null)

            val flow = result.await().getOrThrow()

            val resultList = mutableListOf<ConnectionCandidateEvent>()
            val collectJob = launch(UnconfinedTestDispatcher()) {
                flow.toList(resultList)
            }

            callbackSlot.captured.onEndpointFound(
                "endpointId",
                DiscoveredEndpointInfo("serviceId", "endpointName")
            )
            testScope.advanceUntilIdle()
            callbackSlot.captured.onEndpointFound(
                "endpointId",
                DiscoveredEndpointInfo("serviceId", "endpointName")
            )
            testScope.advanceUntilIdle()
            assertThat(resultList.size, equalTo(1))
            assertThat(resultList[0].type, equalTo(ConnectionEventType.DISCOVERED))
            assertThat(
                resultList[0].candidate,
                IsEqual.equalTo(ConnectionCandidate("endpointId", "endpointName", null))
            )
            collectJob.cancel()
        }

}