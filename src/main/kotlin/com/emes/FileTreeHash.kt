package com.emes

import com.emes.databaseio.DatabaseIO
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.walk
import kotlin.time.measureTimedValue

class FileTreeHash {

    companion object {
        private const val DATABASE_FILE_NAME = ".hashes.csv"
    }

    private val log = KotlinLogging.logger {}
    private val databaseIO = DatabaseIO()
    private val sha3 = SHA3()

    private val hashDispatcher =
        Dispatchers.IO.limitedParallelism(Runtime.getRuntime().availableProcessors())

    private val excludeFilesWith = listOf<(String) -> Boolean>(
        { it -> it.startsWith(".") },
        { it -> it.endsWith(".ods") }
    )

    fun calculateAndCompare(directory: Path): List<FileHashChange> {
        log.info { "Compare hashes in $directory" }
        val currentHashes = measureTimedValue { calculateHashes(directory) }
        log.debug { "Calculating hashes took: ${currentHashes.duration.inWholeMilliseconds}" }

        val databasePath = directory / DATABASE_FILE_NAME
        val changedHashes = if (databasePath.exists()) {
            val previousHashes = databaseIO.read(databasePath)
            compareHashes(previousHashes, currentHashes.value)
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
        databaseIO.write(currentHashes.value, databasePath)
        return changedHashes
    }

    fun calculateForFile(file: Path): String {
        log.info { "Calculate hash for single file: $file" }
        val hash = sha3.calculate(file)
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
                async(hashDispatcher) {
                    FileHash(root.relativize(file), sha3.calculate(file))
                }
            }
            .awaitAll()
    }
}