//package com.changeworld.android_easynearby.impl
//
//import com.changeworld.AndroidStuffMockedTest
//import com.changeworld.android_easynearby.utils.FakeTask
//import io.github.easynearby.core.ConnectionStrategy
//import io.github.easynearby.core.advertising.DeviceInfo
//import io.github.easynearby.core.connection.ConnectionCandidate
//import io.github.easynearby.core.connection.ConnectionCandidateEvent
//import io.github.easynearby.core.connection.ConnectionEventType
//import com.google.android.gms.nearby.connection.ConnectionsClient
//import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
//import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
//import io.mockk.every
//import io.mockk.mockk
//import io.mockk.slot
//import kotlinx.coroutines.async
//import kotlinx.coroutines.flow.toList
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.test.TestScope
//import kotlinx.coroutines.test.UnconfinedTestDispatcher
//import kotlinx.coroutines.test.advanceUntilIdle
//import kotlinx.coroutines.test.runTest
//import org.hamcrest.CoreMatchers
//import org.hamcrest.MatcherAssert
//import org.hamcrest.core.IsEqual
//import org.junit.Before
//import org.junit.Test
//
//class AndroidConnectorAndDiscoverIntegrationTest : AndroidStuffMockedTest() {
//    private lateinit var connectionsClient: ConnectionsClient
//
//
//    @Before
//    fun setUp() {
//        connectionsClient = mockk()
//    }
//
//
//    @Test
//    fun `test When same endpoint found second time after it was disconnected Then `() = runTest {
//        val fakeTask = FakeTask<Void?>()
//        val callbackSlot = slot<EndpointDiscoveryCallback>()
//        every {
//            connectionsClient.startDiscovery(any(), capture(callbackSlot), any())
//        } returns fakeTask
//
//
//        val testScope = TestScope()
//        val discover = AndroidDiscover(testScope, connectionsClient)
//
//        val result = async {
//            discover.startDiscovery(DeviceInfo("name", "id", ConnectionStrategy.STAR))
//        }
//
//        // run async job
//        advanceUntilIdle()
//
//        fakeTask.invokeSuccessListener(null)
//
//        val flow = result.await().getOrThrow()
//
//        val resultList = mutableListOf<ConnectionCandidateEvent>()
//        val collectJob = launch(UnconfinedTestDispatcher()) {
//            flow.toList(resultList)
//        }
//
//        callbackSlot.captured.onEndpointFound(
//            "endpointId",
//            DiscoveredEndpointInfo("serviceId", "endpointName")
//        )
//        testScope.advanceUntilIdle()
//        callbackSlot.captured.onEndpointFound(
//            "endpointId",
//            DiscoveredEndpointInfo("serviceId", "endpointName")
//        )
//        testScope.advanceUntilIdle()
//        MatcherAssert.assertThat(resultList.size, CoreMatchers.equalTo(1))
//        MatcherAssert.assertThat(resultList[0].type, CoreMatchers.equalTo(ConnectionEventType.DISCOVERED))
//        MatcherAssert.assertThat(
//            resultList[0].candidate,
//            IsEqual.equalTo(ConnectionCandidate("endpointId", "endpointName", null))
//        )
//        collectJob.cancel()
//    }
//}