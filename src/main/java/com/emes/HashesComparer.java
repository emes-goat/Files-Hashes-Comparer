package com.emes;

import static java.util.stream.Collectors.groupingBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;

public class HashesComparer {

  private static final Path HASHES_DIRECTORY = Paths.get(".hashes");

  @SneakyThrows
  public void compare(Path directory) {
    Precondition.require(Files.exists(directory), "Directory " + directory + " does not exist");
    Precondition.require(Files.isDirectory(directory),
        "Directory " + directory + " is not a file");

    var database = new Database();
    var filesFromLastTwoScans = database.findFilesFromLastTwoScans(
            directory.resolve(HASHES_DIRECTORY))
        .stream()
        .collect(groupingBy(it -> it.timestamp));

//      var changedFiles = compareHashes(previousHashes, Collections.emptyList());
//      if (!changedFiles.isEmpty()) {
//        System.out.println("Hashes changed for files:");
//        changedFiles.forEach(System.out::println);
//        throw new RuntimeException("Changes in hashes detected");
//      } else {
//        System.out.println("No changes to hashes");
//      }
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
}