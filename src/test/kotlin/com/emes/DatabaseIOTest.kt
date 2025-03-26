package com.emes

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.IOException
import java.nio.file.NoSuchFileException
import kotlin.io.path.Path
import kotlin.io.path.writeBytes

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
    fun writeAndReadEmpty() {
        val databaseIO = DatabaseIO()

        databaseIO.write(emptyList(), databaseFile)

        val actual = databaseIO.read(databaseFile)

        assertIterableEquals(emptyList<FileHash>(), actual)
    }

    @Test
    fun writeAndReadCorruptedHash() {
        val databaseIO = DatabaseIO()

        databaseIO.write(expected, databaseFile)

        databaseFile.flipByteInFile(0)

        assertThrows<IOException> { databaseIO.read(databaseFile) }
    }

    @Test
    fun writeAndReadCorruptedData() {
        val databaseIO = DatabaseIO()

        databaseIO.write(expected, databaseFile)

        databaseFile.flipByteInFile(33) //Hash is first 32 bytes

        assertThrows<IOException> { databaseIO.read(databaseFile) }
    }

    @Test
    fun readNonExistingFile() {
        val databaseIO = DatabaseIO()

        assertThrows<NoSuchFileException> { databaseIO.read(databaseFile) }
    }

    @Test
    fun readEmptyFile() {
        val databaseIO = DatabaseIO()
        databaseFile.writeBytes(ByteArray(0))

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