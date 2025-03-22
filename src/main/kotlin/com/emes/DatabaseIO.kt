package com.emes

import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.writer.CsvWriter
import java.nio.file.Path

class DatabaseIO {

    fun write(fileHashes: List<FileHash>, databaseFile: Path) {
        CsvWriter.builder().build(databaseFile).use { csv ->
            fileHashes.forEach { hashedFile ->
                csv.writeRecord(hashedFile.path.toString(), hashedFile.hash)
            }
        }
    }

    fun read(databaseFile: Path): List<FileHash> {
        return CsvReader.builder().ofCsvRecord(databaseFile).use { csvLines ->
            csvLines.map {
                FileHash(Path.of(it.getField(0)), it.getField(1))
            }
        }
    }
}