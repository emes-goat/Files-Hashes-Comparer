package com.emes

import kotlin.io.path.Path
import kotlin.io.path.isDirectory

fun main(args: Array<String>) {
    require(args.size == 1) { "Directory must be provided in argument" }
    val directoryPath = Path(args[0])
    require(directoryPath.isDirectory()) { "Path is not directory or doesn't exist" }

    HashDB().calculateAndCompare(directoryPath)
}