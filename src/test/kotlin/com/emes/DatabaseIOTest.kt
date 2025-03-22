package com.emes

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.nio.file.NoSuchFileException
import kotlin.io.path.Path

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseIOTest {

    private val databaseFile = Path("database.csv")

    @Test
    fun writeAndRead() {
        val databaseIO = DatabaseIO()
        val expected = listOf(
            FileHash(Path("random file name/in a subdirectory, with comma.jpeg"), "xyz"),
            FileHash(Path("random_fileName\\IN 1234 DIRECTORY.txt"), "abc"),
        )

        databaseIO.write(expected, databaseFile)
        val actual = databaseIO.read(databaseFile)

        assertIterableEquals(expected, actual)
    }

    @Test
    fun readNonExistingFile() {
        cleanup()
        val databaseIO = DatabaseIO()

        assertThrows<NoSuchFileException> { databaseIO.read(databaseFile) }
    }

    @AfterAll
    fun cleanup() {
        databaseFile.delete()
    }
}