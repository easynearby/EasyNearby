package com.changeworld.easynearby.connection.gzip

import com.changeworld.easynearby.connection.Connection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class GzipConnectionDecoratorTest {
    private lateinit var connection: Connection
    private lateinit var gzipConnection: GzipConnection

    @BeforeEach
    fun setUp() {
        connection = mockk(relaxed = true)
        gzipConnection = GzipConnection(connection)
    }

    @Test
    fun `test sendPayload When zipped array is bigger than original one Then pass original data`() =
        runTest {
            val badForGzipPayload = "abcdefg".toByteArray()
            gzipConnection.sendPayload(badForGzipPayload)

            coVerify(exactly = 1) {
                connection.sendPayload(withArg {
                    assertThat(it, equalTo(badForGzipPayload))
                })
            }
        }

    @Test
    fun `test sendPayload When zipped array is less than original one Then pass original data`() =
        runTest {
            val goodForGzip = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".toByteArray()
            gzipConnection.sendPayload(goodForGzip)

            coVerify(exactly = 1) {
                connection.sendPayload(withArg {
                    assertThat(it, `is`(CoreMatchers.not(goodForGzip)))
                })
            }
        }

    @Test
    fun `test getPayload When invoked with zipped value Then pass call to wrapped`() = runTest {
        val originalValue = "originalValue".toByteArray()
        coEvery { connection.getPayload() } returns flow {
            val bos = ByteArrayOutputStream()
            GZIPOutputStream(bos).buffered().use { it.write(originalValue) }
            emit(bos.toByteArray())
        }

        val receivedResult = mutableListOf<ByteArray>()
        launch {
            gzipConnection.getPayload().toList(receivedResult)
        }

        advanceUntilIdle()

        assertThat(receivedResult.size, equalTo(1))
        assertThat(receivedResult.first(), equalTo(originalValue))
    }

    @Test
    fun `test getPayload When invoked with not zipped value Then pass call to wrapped`() = runTest {
        val originalValue = "originalValue".toByteArray()
        coEvery { connection.getPayload() } returns flow {
            emit(originalValue)
        }

        val receivedResult = mutableListOf<ByteArray>()
        launch {
            gzipConnection.getPayload().toList(receivedResult)
        }

        advanceUntilIdle()

        assertThat(receivedResult.size, equalTo(1))
        assertThat(receivedResult.first(), equalTo(originalValue))
    }

    @Test
    fun `test close When invoked Then pass call to wrapped`()= runTest {
        gzipConnection.close()

        coVerify(exactly = 1) {
            connection.close()
        }
    }
}