package com.emes

import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.writer.CsvWriter
import java.io.StringReader
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path

class DatabaseIO {

    private val zstandard = Zstandard()

    fun write(fileHashes: List<FileHash>, databaseFile: Path) {
        val csv = zstandard.compress(serializeToCSV(fileHashes))
        Files.write(databaseFile, csv)
    }

    fun read(databaseFile: Path): List<FileHash> {
        val csv = try {
            zstandard.decompress(Files.readAllBytes(databaseFile))
        } catch (_: Exception) {
            Files.readString(databaseFile)
        }

        return deserializeFromCSV(csv)
    }

    private fun serializeToCSV(fileHashes: List<FileHash>): String {
        val stringWriter = StringWriter()
        CsvWriter.builder().build(stringWriter).use { csv ->
            fileHashes.forEach { hashedFile ->
                csv.writeRecord(hashedFile.path.toString(), hashedFile.hash)
            }
        }
        return stringWriter.toString()
    }

    private fun deserializeFromCSV(csv: String): List<FileHash> {
        val reader = StringReader(csv)
        return CsvReader.builder().ofCsvRecord(reader).use { csvLines ->
            csvLines
                .map {
                    FileHash(Path.of(it.getField(0)), it.getField(1))
                }
                .toList()
        }
    }
}