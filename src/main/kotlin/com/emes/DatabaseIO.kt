package com.emes

import Filehash
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

class DatabaseIO {

    companion object {
        private const val SHA_HASH_LENGTH_BITS = 32
        private const val GZIP_BUFFER_SIZE = 8192
    }

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

        val hashBytes = rawBytes.copyOfRange(0, SHA_HASH_LENGTH_BITS)
        val compressedBytes = rawBytes.copyOfRange(SHA_HASH_LENGTH_BITS, rawBytes.size)
        val compressedBytesHash = compressedBytes.hash()

        if (!compressedBytesHash.contentEquals(hashBytes)) {
            throw DatabaseDataCorruptionException()
        }

        return compressedBytes
            .decompress()
            .deserialize()
    }

    private fun ByteArray.hash(): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(this)
    }

    private fun ByteArray.compress(): ByteArray {
        ByteArrayOutputStream().use { outputStream ->
            GZIPOutputStream(outputStream, GZIP_BUFFER_SIZE).apply {
                write(this@compress)
                close()
            }
            return outputStream.toByteArray()
        }
    }

    private fun ByteArray.decompress(): ByteArray {
        ByteArrayInputStream(this).use { inputStream ->
            GZIPInputStream(inputStream, GZIP_BUFFER_SIZE).use { gzipStream ->
                ByteArrayOutputStream().use { outputStream ->
                    val buffer = ByteArray(GZIP_BUFFER_SIZE)
                    var bytesRead: Int
                    while (gzipStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    return outputStream.toByteArray()
                }
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
        return Filehash.FileHashListProto.parseFrom(this)
            .elementsList
            .map {
                FileHash(
                    path = Paths.get(it.path),
                    hash = it.hash
                )
            }
    }
}