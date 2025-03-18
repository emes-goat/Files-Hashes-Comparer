package com.emes

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class FileTreeHashTest {

    @Test
    fun happyPath() {
        val hashesCalculator = FileTreeHash()
        Files.createDirectory(directory)

        Files.writeString(directory.resolve(fileAName), "Something")
        var actual = hashesCalculator.calculateAndCompare(directory)
        assertEquals(0, actual.size)
        var previousHash = hashesCalculator.calculateForFile(directory.resolve(fileAName))
        Thread.sleep(50)

        val fileAContent = "Welcome to the jungle"
        val fileBContent = "Soft kitty warm kitty"
        Files.writeString(directory.resolve(fileAName), fileAContent)
        Files.writeString(directory.resolve(fileBName), fileBContent)
        actual = hashesCalculator.calculateAndCompare(directory)
        var expected = listOf(
            ChangedHash(
                fileAName, previousHash,
                hashesCalculator.calculateForFile(directory.resolve(fileAName))
            )
        )
        assertIterableEquals(expected, actual)
        previousHash = hashesCalculator.calculateForFile(directory.resolve(fileBName))
        Thread.sleep(50)

        //Doft instead of Soft
        val fileBContentChanged = "Doft kitty warm kitty"
        Files.writeString(directory.resolve(fileBName), fileBContentChanged)
        actual = hashesCalculator.calculateAndCompare(directory)
        expected = listOf(
            ChangedHash(
                fileBName, previousHash,
                hashesCalculator.calculateForFile(directory.resolve(fileBName))
            )
        )
        assertIterableEquals(expected, actual)

        Thread.sleep(50)

        actual = hashesCalculator.calculateAndCompare(directory)
        assertEquals(0, actual.size)
    }

    companion object {
        private val directory: Path = Paths.get("./hellokitty")
        private val fileAName: Path = Paths.get("afile")
        private val fileBName: Path = Paths.get("bfile")

        @BeforeAll
        fun prepare() {
            cleanup()
        }

        @AfterAll
        fun cleanup() {
            if (Files.exists(directory)) {
                Files.walkFileTree(
                    directory,
                    object : SimpleFileVisitor<Path>() {
                        @Throws(IOException::class)
                        override fun postVisitDirectory(
                            dir: Path, exc: IOException?
                        ): FileVisitResult {
                            Files.delete(dir)
                            return FileVisitResult.CONTINUE
                        }

                        @Throws(IOException::class)
                        override fun visitFile(
                            file: Path, attrs: BasicFileAttributes
                        ): FileVisitResult {
                            Files.delete(file)
                            return FileVisitResult.CONTINUE
                        }
                    })
            }
        }
    }
}