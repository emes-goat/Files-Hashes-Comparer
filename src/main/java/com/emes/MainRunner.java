package com.emes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;

public class MainRunner {

  private final ObjectMapper mapper = new ObjectMapper();
  private final AES aes = new AES();
  private final static String HASHES_FILE_NAME = ".hashes";
  private static final int BUFFER_SIZE = 16384;

  @SneakyThrows
  public void run(String password, String directory) {
    check(password, directory);

    var rootDirectory = Paths.get(directory);
    var resultsFile = rootDirectory.resolve(HASHES_FILE_NAME);
    var now = Instant.now();

    System.out.println(MessageFormat.format("Calculate hashes in {0}", directory));
    List<HashedFile> currentResult = getHashes(now, rootDirectory);

    if (Files.exists(resultsFile)) {
      System.out.println("Read saved hashes");
      var previousHashes = readSavedHashes(password, resultsFile);
      var changedFiles = compareHashes(previousHashes, currentResult);
      if (!changedFiles.isEmpty()) {
        System.out.println("Hashes changed for files:");
        changedFiles.forEach(System.out::println);
      } else {
        System.out.println("No changes to hashes");
      }
    }

    // TODO Close the application with error when there is a changed file
    // TODO add more test coverage
    // TODO use file size for file checking

    saveHashes(currentResult, password, resultsFile);
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
        .map(HashedFile::path)
        .toList();
  }

  private List<HashedFile> getHashes(Instant now, Path root) throws IOException {
    var fileHashes = new ArrayList<HashedFile>();

    Files.walkFileTree(root, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        var fileAttributes = Files.readAttributes(file, BasicFileAttributes.class);

        if (!fileAttributes.isDirectory() && fileAttributes.isRegularFile() &&
            fileAttributes.creationTime().toInstant().isBefore(now) &&
            !file.getFileName().toString().startsWith(".")
        ) {
          var result = new HashedFile(root.relativize(file), calculateSHA3(file),
              Files.size(file));

          fileHashes.add(result);
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

  @SneakyThrows
  private void saveHashes(List<HashedFile> hashes, String password, Path file) {
    var serialized = mapper.writeValueAsBytes(hashes);
    var encrypted = aes.encrypt(password.toCharArray(), serialized);
    Files.write(file, encrypted);
  }

  @SneakyThrows
  private List<HashedFile> readSavedHashes(String password, Path resultsFile) {
    var encrypted = Files.readAllBytes(resultsFile);
    var decrypted = aes.decrypt(password.toCharArray(), encrypted);

    return mapper.readValue(decrypted, new TypeReference<>() {
    });
  }

  private void check(String password, String directory) {
    Precondition.require(!password.isBlank(), "Password can't be blank");
    Precondition.require(!directory.isBlank(), "Directory can't be blank");

    var rootDirectory = tryConvertDirectory(directory);
    Precondition.require(Files.exists(rootDirectory), "Directory doesn't exist");
    Precondition.require(Files.isDirectory(rootDirectory), "Directory isn't a directory");
  }

  private Path tryConvertDirectory(String directory) {
    try {
      return Paths.get(directory);
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException(String.format("%s isn't a path", directory));
    }
  }
}