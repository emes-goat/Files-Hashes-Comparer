package com.emes.databaseio

import Filehash.FileHashListProto
import Filehash.FileHashProto
import com.emes.FileHash
import java.nio.file.Paths

fun List<FileHash>.serialize(): ByteArray {
    val builder = FileHashListProto.newBuilder()
    this.forEach { fileHash ->
        builder.addElements(
            FileHashProto.newBuilder()
                .setPath(fileHash.path.toString())
                .setHash(fileHash.hash)
                .build()
        )
    }
    return builder.build().toByteArray()
}

fun ByteArray.deserialize(): List<FileHash> {
    val protoList = FileHashListProto.parseFrom(this)
    return protoList.elementsList.map { proto ->
        FileHash(
            path = Paths.get(proto.path),
            hash = proto.hash
        )
    }
}