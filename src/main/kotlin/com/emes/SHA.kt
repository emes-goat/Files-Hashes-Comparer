package com.emes

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.security.MessageDigest

class SHA {

    companion object {
        private const val HASH_ALGORITHM = "SHA3-256"
        private const val BUFFER_SIZE = 256 * 1024
    }

    private val sha3Digest = ThreadLocal.withInitial {
        MessageDigest.getInstance(HASH_ALGORITHM)
    }
    private val buffer = ThreadLocal.withInitial {
        ByteBuffer.allocateDirect(BUFFER_SIZE)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun calculate(file: Path): String {
        val sha3 = sha3Digest.get().apply { reset() }
        val buffer = buffer.get().apply { clear() }

        return FileChannel.open(file).use { channel ->
            while (channel.read(buffer) > 0) {
                buffer.flip()
                sha3.update(buffer)
                buffer.clear()
            }

            return sha3.digest().toHexString()
        }
    }
}