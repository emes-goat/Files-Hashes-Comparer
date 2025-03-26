package com.emes

import Filehash
import com.github.luben.zstd.ZstdInputStream
import com.github.luben.zstd.ZstdOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

class DatabaseIO {

    fun write(fileHashes: List<FileHash>, databaseFile: Path) {
        val compressed = fileHashes.serialize().compress()

        val hash = compressed.hash()
        val fileContent = hash + compressed

        databaseFile.writeBytes(fileContent)
    }

    fun read(databaseFile: Path): List<FileHash> {
        val rawBytes = databaseFile.readBytes()
        if (rawBytes.isEmpty()) {
            return emptyList()
        }

        val hashBytes = rawBytes.copyOfRange(0, 32)
        val compressedBytes = rawBytes.copyOfRange(32, rawBytes.size)
        val compressedBytesHash = compressedBytes.hash()

        if (!compressedBytesHash.contentEquals(hashBytes)) {
            throw IOException("Data corruption or tampering detected")
        }

        return compressedBytes
            .decompress()
            .deserialize()
    }

    private fun ByteArray.hash(): ByteArray {
        return MessageDigest.getInstance("SHA3-256").digest(this)
    }

    private fun ByteArray.compress(compressionLevel: Int = 12): ByteArray {
        ByteArrayOutputStream().use { outputStream ->
            ZstdOutputStream(outputStream, compressionLevel).use { zstdStream ->
                zstdStream.write(this)
            }

            return outputStream.toByteArray()
        }
    }

    private fun ByteArray.decompress(): ByteArray {
        ByteArrayInputStream(this).use { inputStream ->
            return ZstdInputStream(inputStream).use { zstdStream ->
                zstdStream.readAllBytes()
            }
        }
    }

    private fun List<FileHash>.serialize(): ByteArray {
        val builder = Filehash.FileHashListProto.newBuilder()
        this.forEach { fileHash ->
            builder.addElements(
                Filehash.FileHashProto.newBuilder()
                    .setPath(fileHash.path.toString())
                    .setHash(fileHash.hash)
                    .build()
            )
        }
        return builder.build().toByteArray()
    }

    private fun ByteArray.deserialize(): List<FileHash> {
        val protoList = Filehash.FileHashListProto.parseFrom(this)
        return protoList.elementsList.map { proto ->
            FileHash(
                path = Paths.get(proto.path),
                hash = proto.hash
            )
        }
    }
}