package com.emes

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest
import java.time.Instant
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class FileTreeHash {

    private val log = KotlinLogging.logger {}

    companion object {
        private const val BUFFER_SIZE = 64 * 1024
        private const val DATABASE_FILE_NAME = ".hashes.csv"
        private val THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors()
    }

    fun calculateAndCompare(directory: Path): List<ChangedHash> {
        log.info { "Compare hashes in $directory" }
        val currentHashes = calculateHashes(directory)

        val databasePath = directory.resolve(DATABASE_FILE_NAME)
        val changedHashes = if (Files.exists(databasePath)) {
            val previousHashes = readFromFile(databasePath)
            compareHashes(previousHashes, currentHashes)
        } else {
            log.info { "File with previous hashes doesn't exist" }
            listOf()
        }

        if (changedHashes.isEmpty()) {
            log.info { "OK - no changed hashes" }
        } else {
            log.error { "HASH CHANGED!!!" }
            changedHashes.forEach { log.error { it.toString() } }
        }
        writeToFile(currentHashes, databasePath)
        return changedHashes
    }

    fun calculateForFile(file: Path): String {
        log.info { "${"Calculate hash for single file: {}"} $file" }
        val hash = calculateSHA3(file)
        log.info { "${"Hash: {}"} $hash" }
        return hash
    }

    private fun compareHashes(
        previousHashes: List<HashedFile>,
        currentHashes: List<HashedFile>
    ): List<ChangedHash> {
        return currentHashes
            .mapNotNull { currentFile: HashedFile ->
                previousHashes
                    .filter { previousFile: HashedFile ->
                        currentFile.path == previousFile.path
                                && currentFile.hash != previousFile.hash
                    }
                    .map { previousHash: HashedFile ->
                        ChangedHash(
                            Path.of(currentFile.path), previousHash.hash,
                            currentFile.hash
                        )
                    }
                    .firstOrNull()
            }
    }

    private fun calculateHashes(root: Path): List<HashedFile> {
        val timestamp = Instant.now()

        val eligibleFiles = Files.walk(root)
            .filter { path: Path -> Files.isRegularFile(path) }
            .filter { file: Path ->
                val attrs = Files.readAttributes<BasicFileAttributes>(
                    file,
                    BasicFileAttributes::class.java
                )
                val fileName = file.fileName.toString()
                attrs.lastModifiedTime().toInstant()
                    .isBefore(timestamp) && !fileName.startsWith(".") && !fileName.endsWith(".ods")
            }
            .toList()

        Executors.newFixedThreadPool(THREAD_POOL_SIZE).use { executor ->
            val futures = eligibleFiles.map {
                executor.submit<HashedFile>(Callable {
                    val relativePath = root.relativize(it).toString()
                    val hash = calculateSHA3(it)
                    HashedFile(relativePath, hash)
                })
            }

            return futures.map { it.get() }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun calculateSHA3(file: Path): String {
        val sha3 = MessageDigest.getInstance("SHA3-256")

        FileInputStream(file.toFile()).use { reader ->
            val buffer = ByteArray(BUFFER_SIZE)
            var read: Int = reader.read(buffer, 0, BUFFER_SIZE)
            while (read > -1) {
                sha3.update(buffer, 0, read)
                read = reader.read(buffer, 0, BUFFER_SIZE)
            }
            return sha3.digest().toHexString()
        }
    }

    private fun writeToFile(hashes: List<HashedFile>, databaseFile: Path) {
        val strings = hashes.map { it.toString() }
        Files.write(databaseFile, strings)
    }

    private fun readFromFile(databaseFile: Path): List<HashedFile> {
        return Files.readAllLines(databaseFile)
            .map { HashedFile.fromString(it) }
    }
}