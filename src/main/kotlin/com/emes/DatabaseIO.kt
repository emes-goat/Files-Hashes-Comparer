package com.emes

import Filehash
import com.github.luben.zstd.ZstdInputStream
import com.github.luben.zstd.ZstdOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class DatabaseIO {

    fun write(fileHashes: List<FileHash>, databaseFile: Path) {
        val serialized = fileHashes.serialize().compress()
        Files.write(databaseFile, serialized)
    }

    fun read(databaseFile: Path): List<FileHash> {
        val rawBytes = Files.readAllBytes(databaseFile)
        return rawBytes.decompress().deserialize()
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