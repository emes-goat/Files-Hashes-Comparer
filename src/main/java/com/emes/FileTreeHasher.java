package com.emes;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;

public class FileTreeHasher {

  public List<HashedFile> getHashes(Instant now, Path root) throws IOException {
    var fileHashes = new ArrayList<HashedFile>();

    Files.walkFileTree(root, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        var fileAttributes = Files.readAttributes(file, BasicFileAttributes.class);

        if (!fileAttributes.isDirectory() &&
            fileAttributes.creationTime().toInstant().isBefore(now) &&
            fileAttributes.isRegularFile() &&
            !file.getFileName().toString().startsWith(".")
        ) {
          String hash;
          try (var reader = new FileInputStream(file.toFile())) {
            hash = DigestUtils.sha3_256Hex(reader);
          }

          var relativePath = root.relativize(file);
          var size = Files.size(file);
          var result = new HashedFile(relativePath, hash, size);

          fileHashes.add(result);
        }

        return FileVisitResult.CONTINUE;
      }
    });

    return fileHashes;
  }
}
