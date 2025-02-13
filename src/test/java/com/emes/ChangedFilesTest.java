package com.emes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChangedFilesTest {

  @Test
  public void comparerTest() {
    var comparer = new ChangedFiles();
    var previousHashes = List.of(new HashedFile(Paths.get("path1"), "hash1", 0L),
        new HashedFile(Paths.get("path2"), "hash2", 0L),
        new HashedFile(Paths.get("path3"), "hash3", 0L));

    var currentHashes = List.of(new HashedFile(Paths.get("path1"), "hash1", 0L),
        new HashedFile(Paths.get("path2"), "hash2_changed", 0L),
        new HashedFile(Paths.get("path4"), "hash4", 0L));

    var result = comparer.find(previousHashes, currentHashes);

    var expected = List.of(Paths.get("path2"));

    assertEquals(expected, result);
  }
}