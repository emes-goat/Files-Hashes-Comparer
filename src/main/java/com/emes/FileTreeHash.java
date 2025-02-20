package com.emes;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileTreeHash {

  private static final int BUFFER_SIZE = 16384;
  private static final String DATABASE_FILE_NAME = ".hashes";
  private final Logger log = LogManager.getLogger();

  public List<ChangedHash> calculateAndCompare(Path directory) {
    log.info("Calculate hashes in {}", directory);
    var currentHashes = calculateHashes(directory);
    log.info("Calculated hashes for {} files", currentHashes.size());

    var databasePath = directory.resolve(DATABASE_FILE_NAME);
    var previousHashes = new Database(databasePath).readPreviousHashes();
    List<ChangedHash> changedHashes = List.of();
    if (previousHashes.isEmpty()) {
      log.warn("Previous hashes is empty. Not comparing.");
    } else {
      changedHashes = compareHashes(previousHashes, currentHashes);
      if (changedHashes.isEmpty()) {
        log.info("OK - no changed hashes");
      } else {
        log.error("WARNING - HASH CHANGED!!!");
        changedHashes.forEach(it -> log.error("HASH CHANGED FOR: {}", it));
      }
    }

    new Database(databasePath).saveAll(currentHashes);
    return changedHashes;
  }

  public String calculateForFile(Path file) {
    log.info("Calculate hash for single file: {}", file);
    var hash = calculateSHA3(file);
    log.info("Hash: {}", hash);
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
      var fileHashes = new ArrayList<HashedFile>();
      var timestamp = Instant.now();

      Files.walkFileTree(root, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (Files.getLastModifiedTime(file).toInstant().isBefore(timestamp) &&
              !file.getFileName().toString().startsWith(".")) {

            var hashedFile = new HashedFile(root.relativize(file).toString(), calculateSHA3(file));
            fileHashes.add(hashedFile);
          }
          return FileVisitResult.CONTINUE;
        }
      });

      return fileHashes;
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
}