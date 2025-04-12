package com.emes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.walk

class HashDB {

    companion object {
        private const val HIDDEN_FILE_STARTS_WITH = '.'
        private val N_CPU = Runtime.getRuntime().availableProcessors()
        private const val DATABASE_FILE_NAME = ".hashes"
    }

    private val databaseIO = DatabaseIO()
    private val sha = SHA()

    fun calculateAndCompare(
        directory: Path
    ): List<FileHashChange> {
        println("Compare hashes in $directory")
        val currentHashes = calculateHashes(directory)
        val databaseFile = directory / DATABASE_FILE_NAME

        val changedHashes = if (databaseFile.exists()) {
            val previousHashes = databaseIO.read(databaseFile)
            compareHashes(previousHashes, currentHashes)
        } else {
            println("File with previous hashes doesn't exist")
            emptyList()
        }

        when {
            changedHashes.isEmpty() -> println("OK - No changes detected")
            else -> {
                println("HASH CHANGED!!!")
                changedHashes.forEach { println(it) }
            }
        }
        databaseIO.write(currentHashes, databaseFile)
        return changedHashes
    }

    fun calculateForFile(file: Path): String {
        println("Calculate hash for single file: $file")
        val hash = sha.calculate(file)
        println("Hash: $hash")
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

    private fun calculateHashes(root: Path): List<FileHash> {
        val dispatcher = Dispatchers.IO.limitedParallelism(N_CPU)

        return runBlocking {
            root.walk()
                .filter {
                    !it.fileName.toString().startsWith(HIDDEN_FILE_STARTS_WITH)
                }
                .toList()
                .map { file ->
                    async(dispatcher) {
                        FileHash(root.relativize(file), sha.calculate(file))
                    }
                }
                .awaitAll()
        }
    }
}