package com.emes

import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

@OptIn(ExperimentalPathApi::class)
 fun Path.delete() {
    this.deleteRecursively()
}