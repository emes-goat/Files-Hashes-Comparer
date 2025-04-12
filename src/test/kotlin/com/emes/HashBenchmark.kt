package com.emes

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

open class HashBenchmark {

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
    @Fork(value = 1)
    fun sha2_jdk(blackhole: Blackhole, data: GeneratedBatchData) {
        val digest = MessageDigest.getInstance("SHA-256")
        for (fileBytes in data.filesToHash) {
            val hash = digest.digest(fileBytes)
            blackhole.consume(hash)
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
    @Fork(value = 1)
    fun sha3_jdk(blackhole: Blackhole, data: GeneratedBatchData) {
        val digest = MessageDigest.getInstance("SHA3-256")
        for (fileBytes in data.filesToHash) {
            val hash = digest.digest(fileBytes)
            blackhole.consume(hash)
        }
    }
}