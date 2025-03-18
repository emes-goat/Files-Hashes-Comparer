package com.emes

import java.nio.file.Files
import java.nio.file.Paths

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        require(args.size == 1, "Invalid arguments")
        val location = Paths.get(args[0])

        if (Files.isDirectory(location)) {
            FileTreeHash().calculateAndCompare(location)
        } else if (Files.isRegularFile(location)) {
            FileTreeHash().calculateForFile(location)
        } else {
            throw RuntimeException("Location doesn't exist")
        }
    }

    fun require(condition: Boolean, message: String?) {
        require(condition) { message!! }
    }
}