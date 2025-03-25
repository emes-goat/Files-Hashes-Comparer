package com.emes.databaseio

import com.github.luben.zstd.ZstdInputStream
import com.github.luben.zstd.ZstdOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun ByteArray.compress(compressionLevel: Int = 12): ByteArray {
    ByteArrayOutputStream().use { outputStream ->
        ZstdOutputStream(outputStream, compressionLevel).use { zstdStream ->
            zstdStream.write(this)
        }

        return outputStream.toByteArray()
    }
}

fun ByteArray.decompress(): ByteArray {
    ByteArrayInputStream(this).use { inputStream ->
        return ZstdInputStream(inputStream).use { zstdStream ->
            zstdStream.readAllBytes()
        }
    }
}