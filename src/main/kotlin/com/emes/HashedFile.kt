package com.emes

data class HashedFile(
    val path: String,
    val hash: String
) {
    override fun toString(): String {
        return path + SEPARATOR + hash
    }

    companion object {
        private const val SEPARATOR = ","

        fun fromString(string: String): HashedFile {
            val split = string.split(SEPARATOR)
            return HashedFile(split[0], split[1])
        }
    }
}