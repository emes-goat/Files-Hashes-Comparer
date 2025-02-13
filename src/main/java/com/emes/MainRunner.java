package com.emes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;

public class MainRunner {

  private final Serializer serializer = new Serializer();
  private final AES aes = new AES();
  private final ChangedFiles changedFiles = new ChangedFiles();
  private final static String HASHES_FILE_NAME = ".hashes";

  @SneakyThrows
  public void run(String password, String directory) {
    Precondition.require(!password.isBlank(), "Password can't be blank");
    Precondition.require(!directory.isBlank(), "Directory can't be blank");

    var rootDirectory = tryConvertDirectory(directory);
    Precondition.require(Files.exists(rootDirectory), "Directory doesn't exist");
    Precondition.require(Files.isDirectory(rootDirectory), "Directory isn't an directory");

    var resultsFile = rootDirectory.resolve(HASHES_FILE_NAME);
    var now = Instant.now();
    System.out.println("Calculating hashes");
    List<HashedFile> currentResult = new FileTreeHasher().getHashes(now, rootDirectory);

    var previousResult = readPrevious(password, resultsFile);
    var changedFilesF = changedFiles.find(previousResult, currentResult);
    if (!changedFilesF.isEmpty()) {
      System.out.println("Hashes changed for files:");
      changedFilesF.forEach(System.out::println);
    } else {
      System.out.println("No changes to hashes");
    }

    // TODO Close the application with error when there is a changed file
    // TODO add more test coverage
    // TODO use file size for file checking
    // TODO use logger to handle colors in CLI

    saveCurrent(currentResult, password, resultsFile);
  }

  private Path tryConvertDirectory(String directory) {
    try {
      return Paths.get(directory);
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException(String.format("%s isn't a path", directory));
    }
  }

  private void saveCurrent(List<HashedFile> currentResult, String password, Path resultsFile)
      throws IOException {

    var serialized = serializer.serialize(currentResult);
    var encrypted = aes.encrypt(password.toCharArray(), serialized);
    Files.write(resultsFile, encrypted);
  }

  private List<HashedFile> readPrevious(String password, Path resultsFile)
      throws IOException {

    if (Files.exists(resultsFile)) {
      System.out.println("Previous exists");
      var encrypted = Files.readAllBytes(resultsFile);
      var decrypted = aes.decrypt(password.toCharArray(), encrypted);
      return serializer.deserialize(decrypted);
    } else {
      return Collections.emptyList();
    }
  }
}