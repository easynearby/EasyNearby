package io.github.easynearby.core.discovery

import io.github.easynearby.core.PermissionsChecker
import io.github.easynearby.core.advertising.testimpl.testKoinModule
import io.github.easynearby.core.advertising.utils.DummyData
import io.github.easynearby.core.advertising.utils.setUpConcurrentCalls
import io.github.easynearby.core.di.IsolatedKoinContext
import io.github.easynearby.core.exceptions.PermissionsNotGrantedException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DiscoveryManagerTest {

    companion object {
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

    private lateinit var permissionsChecker: PermissionsChecker
    private lateinit var discover: Discover

    private lateinit var discoveryManager: DiscoveryManager

    @BeforeEach
    fun setUp() {
        permissionsChecker = mockk(relaxed = true)
        discover = mockk(relaxed = true)
        discoveryManager = DiscoveryManager(permissionsChecker, discover)
    }

    @Test
    fun `test startDiscovery When permissions not granted Then return PermissionsNotGrantedException`() =
        runTest {
            val missingPermissions = listOf("permission1", "permission2")
            every { permissionsChecker.getMissingPermissions() } returns missingPermissions

            val result = discoveryManager.startDiscovery(DummyData.deviceInfo)

            assertThat(result.isFailure, equalTo(true))
            assertThat(
                result.exceptionOrNull(),
                instanceOf(PermissionsNotGrantedException::class.java)
            )
            assertThat(
                (result.exceptionOrNull() as PermissionsNotGrantedException).notGrantedPermissions,
                CoreMatchers.equalTo(missingPermissions)
            )
        }

    @Test
    fun `test startDiscovery When permissions granted Then start discovery`() = runTest {
        every { permissionsChecker.hasAllPermissions() } returns true
        coEvery { discover.startDiscovery(DummyData.deviceInfo) } returns Result.success(emptyFlow())

        val result = discoveryManager.startDiscovery(DummyData.deviceInfo)

        assertThat(result.isSuccess, equalTo(true))
        assertThat(result.getOrNull(), Is.`is`(instanceOf(Flow::class.java)))
        coVerify(exactly = 1) { discover.startDiscovery(DummyData.deviceInfo) }
    }

    @Test
    fun `test startDiscovery When has already started Then return IllegalStateException`() =
        runTest {
            every { permissionsChecker.hasAllPermissions() } returns true
            coEvery { discover.startDiscovery(DummyData.deviceInfo) } returns Result.success(
                emptyFlow()
            )

            discoveryManager.startDiscovery(DummyData.deviceInfo)

            val result = discoveryManager.startDiscovery(DummyData.deviceInfo)

            assertThat(result.isFailure, equalTo(true))
            assertThat(
                result.exceptionOrNull(),
                Is.`is`(instanceOf(IllegalStateException::class.java))
            )
        }

    @Test
    fun `test startDiscovery When discovering started from different thread Then return success only once`() =
        runTest {

            val numberOfThreads = 100
            every { permissionsChecker.hasAllPermissions() } returns true
            coEvery { discover.startDiscovery(DummyData.deviceInfo) } returns Result.success(
                emptyFlow()
            )

            val concurrentCalls = setUpConcurrentCalls(numberOfThreads) {
                discoveryManager.startDiscovery(DummyData.deviceInfo)
            }

            val results = concurrentCalls.awaitAll()

            assertThat(results.size, equalTo(numberOfThreads))
            assertThat(results.count { it.isSuccess }, equalTo(1))

            coVerify(exactly = 1) {
                discover.startDiscovery(DummyData.deviceInfo)
            }
        }


    @Test
    fun `test stopDiscovery When discovery has not already started Then do nothing`() = runTest {
        discoveryManager.stopDiscovery()
        coVerify(exactly = 0) {
            discover.stopDiscovery()
        }
    }

    @Test
    fun `test stopDiscovery When discovering started Then invoke discover`() = runTest {
        every { permissionsChecker.hasAllPermissions() } returns true
        coEvery { discover.startDiscovery(DummyData.deviceInfo) } returns Result.success(
            emptyFlow()
        )

        discoveryManager.startDiscovery(DummyData.deviceInfo)

        discoveryManager.stopDiscovery()

        coVerify(exactly = 1) {
            discover.stopDiscovery()
        }
    }

    @Test
    fun `test stopDiscovery When discovering started and stopped Then Do nothing`() = runTest {
        every { permissionsChecker.hasAllPermissions() } returns true
        coEvery { discover.startDiscovery(DummyData.deviceInfo) } returns Result.success(
            emptyFlow()
        )

        discoveryManager.startDiscovery(DummyData.deviceInfo)
        discoveryManager.stopDiscovery()

        discoveryManager.stopDiscovery()

        coVerify(exactly = 1) {
            discover.stopDiscovery()
        }
    }

    @Test
    fun `test stopAdvertising When advertising started and stopped from different thread Then invoke advertiser only once`() =
        runTest {
            val numberOfThreads = 100
            every { permissionsChecker.hasAllPermissions() } returns true
            coEvery { discover.startDiscovery(DummyData.deviceInfo) } returns Result.success(
                emptyFlow()
            )

            discoveryManager.startDiscovery(DummyData.deviceInfo)

            setUpConcurrentCalls(numberOfThreads) {
                discoveryManager.stopDiscovery()
            }.awaitAll()

            coVerify(exactly = 1) {
                discover.stopDiscovery()
            }
        }
}