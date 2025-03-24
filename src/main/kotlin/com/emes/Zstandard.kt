package com.emes

import com.github.luben.zstd.ZstdInputStream
import com.github.luben.zstd.ZstdOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class Zstandard {

    companion object {
        private const val COMPRESSION_LEVEL = 12
        private val charset = Charsets.UTF_8
    }

    fun compress(text: String): ByteArray {
        ByteArrayOutputStream().use { outputStream ->
            ZstdOutputStream(outputStream, COMPRESSION_LEVEL).use { zstdStream ->
                zstdStream.write(text.toByteArray(charset))
            }

            return outputStream.toByteArray()
        }
    }

    fun decompress(byteArray: ByteArray): String {
        ByteArrayInputStream(byteArray).use { inputStream ->
            return ZstdInputStream(inputStream).use { zstdStream ->
                String(zstdStream.readAllBytes(), charset)
            }
        }
    }
}