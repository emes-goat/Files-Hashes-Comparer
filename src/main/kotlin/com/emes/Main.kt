package com.emes

import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

fun main(args: Array<String>) {
    require(args.size == 1) { "Directory or file must be provided in argument" }
    val location = Path(args[0])

    if (location.isDirectory()) {
        HashDB().calculateAndCompare(location)
    } else if (location.isRegularFile()) {
        HashDB().calculateForFile(location)
    } else {
        throw RuntimeException("Location doesn't exist")
    }
}