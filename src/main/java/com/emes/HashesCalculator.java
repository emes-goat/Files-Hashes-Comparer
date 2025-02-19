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
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HashesCalculator {

  private static final int BUFFER_SIZE = 16384;
  private static final String DATABASE_FILE_NAME = ".hashes";
  private final Logger log = LogManager.getLogger();

  public List<Path> run(Path directory) {
    var hashes = calculate(directory);
    var changedHashes = compare(directory, hashes);
    changedHashes.forEach(it -> log.info("HASH CHANGED FOR: {}", it.toString()));

    var databasePath = directory.resolve(DATABASE_FILE_NAME);
    new Database(databasePath).saveAll(hashes);
    return changedHashes;
  }

  @SneakyThrows
  private List<HashedFile> calculate(Path directory) {
    log.info("Calculate hashes in {}", directory);

    var now = Instant.now();
    return calculateHashes(directory, now);
  }

  @SneakyThrows
  private List<Path> compare(Path directory, List<HashedFile> currentHashes) {
    log.info("Compare hashes in {}", directory);

    var previousHashes = new Database(
        directory.resolve(DATABASE_FILE_NAME)).readPreviousHashes();

    if (previousHashes.isEmpty()) {
      log.info("Hashes weren't calculated 2 times. Can't compare two sets. Quitting");
      return List.of();
    }

    return compareHashes(previousHashes, currentHashes);
  }

  private List<Path> compareHashes(List<HashedFile> previousFiles, List<HashedFile> currentFiles) {
    return currentFiles
        .stream()
        .map(currentFile ->
            previousFiles
                .stream()
                .filter(previousFile ->
                    currentFile.path().equals(previousFile.path()) &&
                        !currentFile.hash().equals(previousFile.hash()))
                .findFirst()
                .orElse(null)
        )
        .filter(Objects::nonNull)
        .map(it -> Paths.get(it.path()))
        .toList();
  }

  @SneakyThrows
  private List<HashedFile> calculateHashes(Path root, Instant timestamp) {
    var fileHashes = new ArrayList<HashedFile>();

    Files.walkFileTree(root, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (Files.getLastModifiedTime(file).toInstant().isBefore(timestamp) &&
            !file.getFileName().toString().startsWith(".")) {

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
}