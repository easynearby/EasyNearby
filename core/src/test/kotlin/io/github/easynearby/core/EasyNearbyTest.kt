package io.github.easynearby.core

import io.github.easynearby.core.advertising.AdvertiseManager
import io.github.easynearby.core.advertising.utils.setUpConcurrentCalls
import io.github.easynearby.core.discovery.DiscoveryManager
import io.github.easynearby.core.exceptions.EasyNearbyNotInitializedException
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertThrows

/**
 * Need TestMethodOrder(OrderAnnotation::class) to run not initialized test cases first
 */
@TestMethodOrder(OrderAnnotation::class)
class EasyNearbyTest {

    @Order(1)
    @Test
    fun `test getAdvertiseManager When EasyNearby not initialized Then throw EasyNearbyNotInitializedException`() {
        assertThrows<EasyNearbyNotInitializedException> {

            EasyNearby.getAdvertiseManager()
        }
    }

    @Order(2)
    @Test
    fun `test getDiscoverManager When EasyNearby not initialized Then throw EasyNearbyNotInitializedException`() {
        assertThrows<EasyNearbyNotInitializedException> {

            EasyNearby.getDiscoverManager()
        }
    }

    @Order(3)
    @Test
    fun `test initialize When FactoryLoader throws Exception Then throw EasyNearbyNotInitializedException`() {
        mockEasyNearbyInitialization()

        every { FactoryLoader.loadFactory() } throws RuntimeException("FactoryLoader failed")

        assertThrows<EasyNearbyNotInitializedException> {
            EasyNearby.initialize()
        }

        unmockEasyNearbyInitialization()
    }

    @Order(4)
    @Test
    fun `test initalize When called several times Then initialize only once`() = runTest {
        mockEasyNearbyInitialization()
        setUpConcurrentCalls(20) {
            EasyNearby.initialize()
        }.awaitAll()

        verify(exactly = 1) {
            FactoryLoader.loadFactory()
        }

        unmockEasyNearbyInitialization()
    }

    @Test
    fun `test getAdvertiseManager When EasyNearby initialized Then returns AdvertiseManager`() {
        mockEasyNearbyInitialization()
        EasyNearby.initialize()

        assertThat(EasyNearby.getAdvertiseManager(), `is`(instanceOf(AdvertiseManager::class.java)))

        unmockEasyNearbyInitialization()
    }

    @Test
    fun `test getDiscoverManager When EasyNearby initialized Then returns DiscoveryManager`() {
        mockEasyNearbyInitialization()
        EasyNearby.initialize()

        assertThat(EasyNearby.getDiscoverManager(), `is`(instanceOf(DiscoveryManager::class.java)))

        unmockEasyNearbyInitialization()
    }

    private fun mockEasyNearbyInitialization() {
        mockkObject(FactoryLoader)
//        val factory = mockk<EasyNearbyInitializerFactory>(relaxed = true)
//        every { factory.createLogger() } returns mockk(relaxed = true, relaxUnitFun = true)
        every { FactoryLoader.loadFactory() } returns mockk(relaxed = true)
    }

    private fun unmockEasyNearbyInitialization() {
        unmockkObject(FactoryLoader)
    }
}