package com.emes;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HashesCalculator {

  private static final int BUFFER_SIZE = 16384;
  private static final Path DATABASE_FILE_NAME = Paths.get(".hashes");
  private final Logger log = LogManager.getLogger();

  @SneakyThrows
  public void calculate(Path directory) {
    check(directory);
    log.info("Calculate hashes in {}", directory);

    var databasePath = directory.resolve(DATABASE_FILE_NAME);
    var now = Instant.now();
    var hashes = calculateHashes(directory, now, databasePath);

    new Database().saveAll(hashes, databasePath);
  }

  @SneakyThrows
  private List<HashedFile> calculateHashes(Path root, Instant timestamp, Path databasePath) {
    var fileHashes = new ArrayList<HashedFile>();

    Files.walkFileTree(root, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (Files.getLastModifiedTime(file).toInstant().isBefore(timestamp) &&
            !file.equals(databasePath)) {

          var hashedFile = new HashedFile(root.relativize(file).toString(), calculateSHA3(file),
              timestamp);
          fileHashes.add(hashedFile);
        }
        return FileVisitResult.CONTINUE;
      }
    });

    return fileHashes;
  }

  @SneakyThrows
  private String calculateSHA3(Path file) {
    var sha3 = MessageDigest.getInstance("SHA3-256");

    try (var reader = new FileInputStream(file.toFile())) {
      byte[] buffer = new byte[BUFFER_SIZE];

      for (int read = reader.read(buffer, 0, BUFFER_SIZE); read > -1;
          read = reader.read(buffer, 0, BUFFER_SIZE)) {
        sha3.update(buffer, 0, read);
      }

      return HexFormat.of().formatHex(sha3.digest());
    }
  }

  private void check(Path directory) {
    Precondition.require(Files.exists(directory), "Directory doesn't exist");
    Precondition.require(Files.isDirectory(directory), "Directory isn't a directory");
  }
}