package com.emes

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Instant
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.walk

class FileTreeHash {

    companion object {
        private const val BUFFER_SIZE = 64 * 1024
        private const val DATABASE_FILE_NAME = ".hashes.csv"
        private const val HASH_ALGORITHM = "SHA3-256"
    }

    private val log = KotlinLogging.logger {}
    private val databaseIO = DatabaseIO()

    private val threadPoolSize = Runtime.getRuntime().availableProcessors()
    private val excludeFilesWith = listOf<(String) -> Boolean>(
        { it -> it.startsWith(".") },
        { it -> it.endsWith(".ods") }
    )

    fun calculateAndCompare(directory: Path): List<FileHashChange> {
        log.info { "Compare hashes in $directory" }
        val currentHashes = calculateHashes(directory)

        val databasePath = directory / DATABASE_FILE_NAME
        val changedHashes = if (databasePath.exists()) {
            val previousHashes = databaseIO.read(databasePath)
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
        databaseIO.write(currentHashes, databasePath)
        return changedHashes
    }

    fun calculateForFile(file: Path): String {
        log.info { "Calculate hash for single file: $file" }
        val hash = calculateSHA3(file)
        log.info { "Hash: $hash" }
        return hash
    }

    private fun compareHashes(
        previousHashes: List<FileHash>,
        currentHashes: List<FileHash>
    ): List<FileHashChange> {
        return currentHashes
            .mapNotNull { currentFile: FileHash ->
                previousHashes
                    .filter { previousFile: FileHash ->
                        currentFile.path == previousFile.path
                                && currentFile.hash != previousFile.hash
                    }
                    .map { previousHash: FileHash ->
                        FileHashChange(
                            currentFile.path, previousHash.hash,
                            currentFile.hash
                        )
                    }
                    .firstOrNull()
            }
    }

    private fun calculateHashes(root: Path): List<FileHash> = runBlocking {
        val timestamp = Instant.now()

        val eligibleFiles = root.walk()
            .filter { file ->
                file.getLastModifiedTime().toInstant().isBefore(timestamp) &&
                        excludeFilesWith.none { it(file.fileName.toString()) }
            }
            .toList()

        eligibleFiles
            .map { file ->
                CoroutineScope(Dispatchers.IO.limitedParallelism(threadPoolSize)).async {
                    FileHash(root.relativize(file), calculateSHA3(file))
                }
            }
            .awaitAll()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun calculateSHA3(file: Path): String {
        val sha3 = MessageDigest.getInstance(HASH_ALGORITHM)

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
}