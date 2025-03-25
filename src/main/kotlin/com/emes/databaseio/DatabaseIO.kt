package com.emes.databaseio

import com.emes.FileHash
import java.nio.file.Files
import java.nio.file.Path

class DatabaseIO {

    fun write(fileHashes: List<FileHash>, databaseFile: Path) {
        val serialized = fileHashes.serialize().compress()
        Files.write(databaseFile, serialized)
    }

    fun read(databaseFile: Path): List<FileHash> {
        val rawBytes = Files.readAllBytes(databaseFile)
        return rawBytes.decompress().deserialize()
    }
}