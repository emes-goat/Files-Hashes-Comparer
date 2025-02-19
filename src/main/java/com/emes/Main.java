package com.emes;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

  public static void main(String[] args) {
    var directory = Paths.get(args[0]);
    require(Files.exists(directory), "Directory doesn't exist");
    require(Files.isDirectory(directory), "Directory isn't a directory");

    var hashesCalculator = new HashesCalculator();
    hashesCalculator.calculate(directory);
    hashesCalculator.compare(directory);
  }

  public static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalArgumentException(message);
    }
  }
}