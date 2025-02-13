package com.emes;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ChangedFiles {

  public List<Path> find(List<HashedFile> previousResult, List<HashedFile> currentResult) {
    return currentResult
        .stream()
        .map(currentFileHash -> {
          var matchingByPath = find(previousResult,
              (it) -> it.path().equals(currentFileHash.path()) &&
                  !it.hash().equals(currentFileHash.hash()));

          if (matchingByPath != null) {
            return matchingByPath.path();
          } else {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .toList();
  }

  private HashedFile find(List<HashedFile> collection, Predicate<HashedFile> searchFunction) {
    return collection
        .stream()
        .filter(searchFunction)
        .findFirst()
        .orElse(null);
  }
}

