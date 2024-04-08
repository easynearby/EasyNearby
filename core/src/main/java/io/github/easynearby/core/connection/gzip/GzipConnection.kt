package io.github.easynearby.core.connection.gzip

import io.github.easynearby.core.connection.Connection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipException

/**
 * Gzip connection wrapper. It tries to compress the payload if it's less than the original one.
 */
internal class GzipConnection(private val wrapped: Connection) : Connection {

    override val id: String
        get() = wrapped.id

    override val name: String
        get() = wrapped.name

    override suspend fun sendPayload(payload: ByteArray): Result<Unit> {
        val zipped = gzip(payload)
        return if (zipped.size < payload.size) {
            wrapped.sendPayload(zipped)
        } else {
            wrapped.sendPayload(payload)
        }
    }

    override suspend fun getPayload(): Flow<ByteArray> {
        return wrapped.getPayload().map {
            try {
                unGzip(it)
            } catch (e: ZipException) {
                it
            }
        }
    }

    override suspend fun close() {
        wrapped.close()
    }

    private fun gzip(content: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).buffered().use { it.write(content) }
        return bos.toByteArray()
    }

    @Throws(ZipException::class)
    private fun unGzip(content: ByteArray): ByteArray {
        return GZIPInputStream(content.inputStream()).buffered().use { it.readBytes() }
    }
}