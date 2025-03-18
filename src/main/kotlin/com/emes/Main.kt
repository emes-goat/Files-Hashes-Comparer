package com.emes;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

  public static void main(String[] args) {
    require(args.length == 1, "Invalid arguments");
    var location = Paths.get(args[0]);

    if (Files.isDirectory(location)) {
      new FileTreeHash().calculateAndCompare(location);
    } else if (Files.isRegularFile(location)) {
      new FileTreeHash().calculateForFile(location);
    } else {
      throw new RuntimeException("Location doesn't exist");
    }
  }

  public static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalArgumentException(message);
    }
  }
}