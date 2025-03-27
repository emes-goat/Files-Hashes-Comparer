package com.emes

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.writeText

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HashDBTest {

    private val directory = Path("./hellokitty")
    private val fileAName = Path("afile")
    private val fileAPath = directory.resolve(fileAName)
    private val fileBName = Path("bfile")
    private val fileBPath = directory.resolve(fileBName)

    @Test
    fun happyPath() {
        val hashesCalculator = HashDB()
        directory.createDirectory()

        directory.resolve(fileAName).writeText("Something")
        var actual = hashesCalculator.calculateAndCompare(directory)
        assertEquals(0, actual.size)
        var previousHash = hashesCalculator.calculateForFile(fileAPath)
        Thread.sleep(20)

        fileAPath.writeText("Welcome to the jungle")
        fileBPath.writeText("Soft kitty warm kitty")
        actual = hashesCalculator.calculateAndCompare(directory)
        var expected = listOf(
            FileHashChange(
                fileAName, previousHash,
                hashesCalculator.calculateForFile(fileAPath)
            )
        )
        assertIterableEquals(expected, actual)
        previousHash = hashesCalculator.calculateForFile(fileBPath)
        Thread.sleep(20)

        //Doft instead of Soft
        fileBPath.writeText("Doft kitty warm kitty")
        actual = hashesCalculator.calculateAndCompare(directory)
        expected = listOf(
            FileHashChange(
                fileBName, previousHash,
                hashesCalculator.calculateForFile(fileBPath)
            )
        )
        assertIterableEquals(expected, actual)

        Thread.sleep(20)

        actual = hashesCalculator.calculateAndCompare(directory)
        assertEquals(0, actual.size)
    }

    @AfterAll
    fun cleanup() {
        directory.delete()
    }
}