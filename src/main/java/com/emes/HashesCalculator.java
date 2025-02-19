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

  @SneakyThrows
  public void calculate(Path directory) {
    check(directory);
    log.info("Calculate hashes in {}", directory);

    var databasePath = directory.resolve(DATABASE_FILE_NAME);
    var now = Instant.now();
    var hashes = calculateHashes(directory, now);

    new Database(databasePath).saveAll(hashes);
  }

  @SneakyThrows
  public List<Path> compare(Path directory) {
    check(directory);
    log.info("Compare hashes in {}", directory);

    var filesFromLastTwoScans = new Database(
        directory.resolve(DATABASE_FILE_NAME)).findFilesFromLastTwoScans();
    var howManyHashes = filesFromLastTwoScans.stream()
        .map(it -> it.timestamp)
        .distinct()
        .sorted(Instant::compareTo)
        .toList();

    if (howManyHashes.size() != 2) {
      log.info("Hashes weren't calculated 2 times. Can't compare two sets. Quitting");
      return List.of();
    }

    var newer = filesFromLastTwoScans.stream()
        .filter(it -> it.timestamp.equals(howManyHashes.getFirst()))
        .toList();

    var older = filesFromLastTwoScans.stream()
        .filter(it -> it.timestamp.equals(howManyHashes.getLast()))
        .toList();

    var changedHashes = compareHashes(older, newer);
    changedHashes.forEach(it -> log.info("HASH CHANGED FOR: {}", it.toString()));
    return changedHashes;
  }

  private List<Path> compareHashes(List<HashedFile> previousFiles, List<HashedFile> currentFiles) {
    return currentFiles
        .stream()
        .map(currentFile ->
            previousFiles
                .stream()
                .filter(previousFile ->
                    currentFile.path.equals(previousFile.path) &&
                        !currentFile.hash.equals(previousFile.hash))
                .findFirst()
                .orElse(null)
        )
        .filter(Objects::nonNull)
        .map(it -> Paths.get(it.path))
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

  private void check(Path directory) {
    Precondition.require(Files.exists(directory), "Directory doesn't exist");
    Precondition.require(Files.isDirectory(directory), "Directory isn't a directory");
  }
}