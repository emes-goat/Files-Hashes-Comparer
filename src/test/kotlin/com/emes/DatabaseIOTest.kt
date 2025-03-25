package com.emes

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.nio.file.NoSuchFileException
import kotlin.io.path.Path
import kotlin.io.path.writeText

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseIOTest {

    private val databaseFile = Path("database.csv")
    private val expected = listOf(
        FileHash(Path("random file name/in a subdirectory, with comma.jpeg"), "xyz"),
        FileHash(Path("random_fileName\\IN 1234 DIRECTORY.txt"), "abc"),
    )

    @Test
    fun writeAndRead() {
        val databaseIO = DatabaseIO()

        databaseIO.write(expected, databaseFile)

        val actual = databaseIO.read(databaseFile)

        assertIterableEquals(expected, actual)
    }

    @Test
    fun readNonExistingFile() {
        val databaseIO = DatabaseIO()

        assertThrows<NoSuchFileException> { databaseIO.read(databaseFile) }
    }

    @Test
    fun readEmptyCSVFile() {
        val databaseIO = DatabaseIO()
        databaseFile.writeText("")

        val actual = databaseIO.read(databaseFile)
        assertTrue(actual.isEmpty())
    }

    @AfterAll
    fun cleanup() {
        databaseFile.delete()
    }

    @BeforeEach
    fun cleanupBeforeEach() {
        databaseFile.delete()
    }
}