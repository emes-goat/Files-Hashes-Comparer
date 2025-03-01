package com.emes;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileTreeHash {

  private static final int BUFFER_SIZE = 64 * 1024;
  private static final String DATABASE_FILE_NAME = ".hashes.csv";
  private static final Logger LOGGER = LogManager.getLogger();
  private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

  public List<ChangedHash> calculateAndCompare(Path directory) {
    LOGGER.info("Compare hashes in {}", directory);
    var currentHashes = calculateHashes(directory);

    var databasePath = directory.resolve(DATABASE_FILE_NAME);

    List<ChangedHash> changedHashes = List.of();
    if (!Files.exists(databasePath)) {
      LOGGER.info("File with previous hashes doesn't exist");
    } else {
      var previousHashes = readFromFile(databasePath);
      changedHashes = compareHashes(previousHashes, currentHashes);
      if (changedHashes.isEmpty()) {
        LOGGER.info("OK - no changed hashes");
      } else {
        LOGGER.error("HASH CHANGED!!!");
        changedHashes.forEach(it -> LOGGER.error(it.toString()));
      }
    }

    saveToFile(currentHashes, databasePath);
    return changedHashes;
  }

  public String calculateForFile(Path file) {
    LOGGER.info("Calculate hash for single file: {}", file);
    var hash = calculateSHA3(file);
    LOGGER.info("Hash: {}", hash);
    return hash;
  }

  private List<ChangedHash> compareHashes(List<HashedFile> previousHashes,
      List<HashedFile> currentHashes) {

    return currentHashes
        .stream()
        .map(currentFile ->
            previousHashes
                .stream()
                .filter(previousFile ->
                    currentFile.path().equals(previousFile.path()) &&
                        !currentFile.hash().equals(previousFile.hash()))
                .map(previousHash ->
                    new ChangedHash(Path.of(currentFile.path()), previousHash.hash(),
                        currentFile.hash()))
                .findFirst()
                .orElse(null)
        )
        .filter(Objects::nonNull)
        .toList();
  }

  private List<HashedFile> calculateHashes(Path root) {
    try {
      var timestamp = Instant.now();

      List<Path> eligibleFiles = Files.walk(root)
          .filter(Files::isRegularFile)
          .filter(file -> {
            try {
              var attrs = Files.readAttributes(file, BasicFileAttributes.class);
              var fileName = file.getFileName().toString();
              return attrs.lastModifiedTime().toInstant().isBefore(timestamp) &&
                  !fileName.startsWith(".") && !fileName.endsWith(".ods");
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          })
          .toList();

      List<Future<HashedFile>> futures;
      try (var executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
        futures = eligibleFiles.stream()
            .map(file -> executor.submit(() -> {
              var relativePath = root.relativize(file).toString();
              var hash = calculateSHA3(file);
              return new HashedFile(relativePath, hash);
            }))
            .toList();

        return futures.stream()
            .map(it -> {
              try {
                return it.get();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
            .toList();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String calculateSHA3(Path file) {
    try {
      var sha3 = MessageDigest.getInstance("SHA3-256");

      try (var reader = new FileInputStream(file.toFile())) {
        byte[] buffer = new byte[BUFFER_SIZE];

        for (int read = reader.read(buffer, 0, BUFFER_SIZE); read > -1;
            read = reader.read(buffer, 0, BUFFER_SIZE)) {
          sha3.update(buffer, 0, read);
        }

        return HexFormat.of().formatHex(sha3.digest());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void saveToFile(List<HashedFile> hashes, Path databaseFile) {
    try {
      var strings = hashes.stream().map(Record::toString).toList();
      Files.write(databaseFile, strings);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<HashedFile> readFromFile(Path databaseFile) {
    try {
      return Files.readAllLines(databaseFile)
          .stream()
          .map(it -> it.split(","))
          .map(it -> new HashedFile(it[0], it[1]))
          .toList();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}