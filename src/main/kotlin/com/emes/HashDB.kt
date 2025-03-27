package com.emes

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.walk

class HashDB {

    private val log = KotlinLogging.logger {}
    private val databaseIO = DatabaseIO()
    private val sha3 = SHA3()

    private val hashDispatcher =
        Dispatchers.IO.limitedParallelism(Runtime.getRuntime().availableProcessors())

    private val excludeFilesWith = listOf<(String) -> Boolean>(
        { it -> it.startsWith(".") },
        { it -> it.endsWith(".ods") }
    )

    fun calculateAndCompare(
        directory: Path,
        databaseFileName: String = ".hashes"
    ): List<FileHashChange> {
        log.info { "Compare hashes in $directory" }
        val currentHashes = calculateHashes(directory)

        val databasePath = directory / databaseFileName
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
        val hash = sha3.calculate(file)
        log.info { "Hash: $hash" }
        return hash
    }

    private fun compareHashes(
        previousHashes: List<FileHash>,
        currentHashes: List<FileHash>
    ): List<FileHashChange> {
        val previousMap = previousHashes.associateBy { it.path }

        return currentHashes.mapNotNull { current ->
            previousMap[current.path]
                ?.takeIf { it.hash != current.hash }
                ?.let { previous ->
                    FileHashChange(
                        path = current.path,
                        previous = previous.hash,
                        current = current.hash
                    )
                }
        }
    }

    private fun calculateHashes(root: Path): List<FileHash> = runBlocking {
        root.walk()
            .filter { file ->
                excludeFilesWith.none { it(file.fileName.toString()) }
            }
            .toList()
            .map { file ->
                async(hashDispatcher) {
                    FileHash(root.relativize(file), sha3.calculate(file))
                }
            }
            .awaitAll()
    }
}