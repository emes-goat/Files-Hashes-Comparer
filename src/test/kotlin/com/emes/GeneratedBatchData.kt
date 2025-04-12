package com.emes

import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import kotlin.random.Random


@State(Scope.Benchmark)
open class GeneratedBatchData {

    companion object {
        private const val FILE_SIZE = 1048576
        private const val NUM_FILES = 1300
        private const val TOTAL_SIZE_MB = (NUM_FILES.toLong() * FILE_SIZE) / (1024 * 1024)
    }

    lateinit var filesToHash: List<ByteArray>
        private set

    @Setup(Level.Trial)
    fun setUp() {
        println("Setting up $NUM_FILES 'files', each $FILE_SIZE bytes (Total: ~$TOTAL_SIZE_MB MB)...")

        val tempList = ArrayList<ByteArray>(NUM_FILES)
        val random = Random.Default

        for (i in 0 until NUM_FILES) {
            val fileData = ByteArray(FILE_SIZE)
            random.nextBytes(fileData)
            tempList.add(fileData)
        }
        filesToHash = tempList

        println("Data setup complete")
    }
}