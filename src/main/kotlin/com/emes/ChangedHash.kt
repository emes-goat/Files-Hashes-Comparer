package com.emes;

import java.nio.file.Path;

public record ChangedHash(
    Path path,
    String previous,
    String current
) {

  @Override
  public String toString() {
    return path + " " + previous + " -> " + current;
  }
}
