package com.emes

import java.nio.file.Path

data class ChangedHash(
    val path: Path,
    val previous: String,
    val current: String
) {
    override fun toString(): String {
        return "$path $previous -> $current"
    }
}
