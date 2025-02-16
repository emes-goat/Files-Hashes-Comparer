package com.emes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.SneakyThrows;

public class HashesComparer {

  private static final Path HASHES_DIRECTORY = Paths.get(".hashes");

  @SneakyThrows
  public void compare(Path directory) {
    Precondition.require(Files.exists(directory), "Directory " + directory + " does not exist");
    Precondition.require(Files.isDirectory(directory),
        "Directory " + directory + " is not a file");

    var database = new Database();
    database.findLastTwoTimestamps(directory.resolve(HASHES_DIRECTORY));

//    if (Files.exists(resultsFile)) {
//      System.out.println("Read saved hashes");
//      var previousHashes = readSavedHashes(resultsFile);
//      var changedFiles = compareHashes(previousHashes, Collections.emptyList());
//      if (!changedFiles.isEmpty()) {
//        System.out.println("Hashes changed for files:");
//        changedFiles.forEach(System.out::println);
//        throw new RuntimeException("Changes in hashes detected");
//      } else {
//        System.out.println("No changes to hashes");
//      }
//    }
  }

  // TODO Close the application with error when there is a changed file

//  private List<Path> compareHashes(List<HashedFile> previousFiles, List<HashedFile> currentFiles) {
//    return currentFiles
//        .stream()
//        .map(currentFile ->
//            previousFiles
//                .stream()
//                .filter(previousFile ->
//                    currentFile.file().equals(previousFile.file()) &&
//                        !currentFile.hash().equals(previousFile.hash()))
//                .findFirst()
//                .orElse(null)
//        )
//        .filter(Objects::nonNull)
//        .map(HashedFile::file)
//        .toList();
//  }

  private void check(Path directory) {
    Precondition.require(Files.exists(directory), "Directory doesn't exist");
    Precondition.require(Files.isDirectory(directory), "Directory isn't a directory");
  }
}