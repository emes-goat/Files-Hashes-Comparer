package com.emes

import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

@OptIn(ExperimentalPathApi::class)
fun Path.delete() {
    this.deleteRecursively()
}

fun Path.flipByteInFile(byte: Int) {
    val bytes = this.readBytes().toMutableList()
    bytes[byte] = (bytes[byte] + 1).toByte()

    this.writeBytes(bytes.toByteArray())
}