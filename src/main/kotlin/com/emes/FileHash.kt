package com.emes

import java.nio.file.Path

// Public API do not change
data class FileHash(
    val path: Path,
    val hash: String
)