package com.emes

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*

class SHA {

    companion object {
        private const val HASH_ALGORITHM = "SHA-256"
        private const val BUFFER_SIZE = 256 * 1024
    }

    private val shaDigest = ThreadLocal.withInitial {
        MessageDigest.getInstance(HASH_ALGORITHM)
    }
    private val buffer = ThreadLocal.withInitial {
        ByteBuffer.allocateDirect(BUFFER_SIZE)
    }

    fun calculate(file: Path): String {
        val sha = shaDigest.get().apply { reset() }
        val buffer = buffer.get().apply { clear() }

        return FileChannel.open(file).use { channel ->
            while (channel.read(buffer) > 0) {
                buffer.flip()
                sha.update(buffer)
                buffer.clear()
            }

            return HexFormat.of().formatHex(sha.digest())
        }
    }
}