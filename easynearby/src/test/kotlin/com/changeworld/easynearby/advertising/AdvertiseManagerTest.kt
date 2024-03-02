package com.changeworld.easynearby.advertising

import com.changeworld.easynearby.PermissionsChecker
import com.changeworld.easynearby.advertising.testimpl.testKoinModule
import com.changeworld.easynearby.advertising.utils.DummyData
import com.changeworld.easynearby.advertising.utils.setUpConcurrentCalls
import com.changeworld.easynearby.di.IsolatedKoinContext
import com.changeworld.easynearby.exceptions.PermissionsNotGrantedException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AdvertiseManagerTest {

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
    private lateinit var advertiser: Advertiser

    private lateinit var advertiseManager: AdvertiseManager

    @BeforeEach
    fun setUp() {
        permissionsChecker = mockk(relaxed = true)
        advertiser = mockk(relaxed = true)
        advertiseManager = AdvertiseManager(permissionsChecker, advertiser)
    }

    @Test
    fun `test startAdvertising When permissions not granted Then return PermissionsNotGrantedException`() =
        runTest {
            val missingPermissions = listOf("permission1", "permission2")
            every { permissionsChecker.getMissingPermissions() } returns missingPermissions

            val result = advertiseManager.startAdvertising(DummyData.deviceInfo)

            assertThat(result.isFailure, equalTo(true))
            assertThat(
                result.exceptionOrNull(),
                `is`(instanceOf(PermissionsNotGrantedException::class.java))
            )
            assertThat(
                (result.exceptionOrNull() as PermissionsNotGrantedException).notGrantedPermissions,
                equalTo(missingPermissions)
            )
        }

    @Test
    fun `test startAdvertising When advertising has not already started Then invoke advertiser and return Flow`() =
        runTest {
            every { permissionsChecker.hasAllPermissions() } returns true
            coEvery { advertiser.startAdvertising(DummyData.deviceInfo) } returns Result.success(
                emptyFlow()
            )

            val result = advertiseManager.startAdvertising(DummyData.deviceInfo)

            assertThat(result.isSuccess, equalTo(true))
            assertThat(result.getOrNull(), `is`(instanceOf(Flow::class.java)))

            coVerify(exactly = 1) {
                advertiser.startAdvertising(DummyData.deviceInfo)
            }
        }

    @Test
    fun `test startAdvertising When advertising already started Then return IllegalStateException`() =
        runTest {
            every { permissionsChecker.hasAllPermissions() } returns true

            advertiseManager.startAdvertising(DummyData.deviceInfo)

            val result = advertiseManager.startAdvertising(DummyData.deviceInfo)

            assertThat(result.isFailure, equalTo(true))
            assertThat(
                result.exceptionOrNull(), `is`(instanceOf(IllegalStateException::class.java))
            )
        }

    @Test
    fun `test stopAdvertising When advertising started from different thread Then return success only once`() =
        runTest {
            val numberOfThreads = 100
            every { permissionsChecker.hasAllPermissions() } returns true
            coEvery { advertiser.startAdvertising(DummyData.deviceInfo) } returns Result.success(
                emptyFlow()
            )

            val concurrentCalls = setUpConcurrentCalls(numberOfThreads) {
                advertiseManager.startAdvertising(DummyData.deviceInfo)
            }

            val results = concurrentCalls.awaitAll()

            assertThat(results.size, equalTo(numberOfThreads))
            assertThat(results.count { it.isSuccess }, equalTo(1))

            coVerify(exactly = 1) {
                advertiser.startAdvertising(DummyData.deviceInfo)
            }
        }

    @Test
    fun `test stopAdvertising When advertising not started Then do nothing`() =
        runTest {
            advertiseManager.stopAdvertising()

            coVerify(exactly = 0) {
                advertiser.stopAdvertising()
            }
        }

    @Test
    fun `test stopAdvertising When advertising started Then invoke advertiser`() = runTest {
        every { permissionsChecker.hasAllPermissions() } returns true
        coEvery { advertiser.startAdvertising(DummyData.deviceInfo) } returns Result.success(
            emptyFlow()
        )

        advertiseManager.startAdvertising(DummyData.deviceInfo)

        advertiseManager.stopAdvertising()

        coVerify(exactly = 1) {
            advertiser.stopAdvertising()
        }
    }

    @Test
    fun `test stopAdvertising When advertising started and stopped Then Do nothing`() = runTest {
        every { permissionsChecker.hasAllPermissions() } returns true
        coEvery { advertiser.startAdvertising(DummyData.deviceInfo) } returns Result.success(
            emptyFlow()
        )

        advertiseManager.startAdvertising(DummyData.deviceInfo)

        advertiseManager.stopAdvertising()
        advertiseManager.stopAdvertising()
        advertiseManager.stopAdvertising()

        coVerify(exactly = 1) {
            advertiser.stopAdvertising()
        }
    }

    @Test
    fun `test stopAdvertising When advertising started and stopped from different thread Then invoke advertiser only once`() =
        runTest {
            val numberOfThreads = 100
            every { permissionsChecker.hasAllPermissions() } returns true
            coEvery { advertiser.startAdvertising(DummyData.deviceInfo) } returns Result.success(
                emptyFlow()
            )

            advertiseManager.startAdvertising(DummyData.deviceInfo)

            setUpConcurrentCalls(numberOfThreads) {
                advertiseManager.stopAdvertising()
            }.awaitAll()

            coVerify(exactly = 1) {
                advertiser.stopAdvertising()
            }
        }
}