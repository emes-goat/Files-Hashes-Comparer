package com.emes;

public record HashedFile(
    String path,
    String hash
) {

  private static final String SEPARATOR = ",";

  @Override
  public String toString() {
    return path + SEPARATOR + hash;
  }

  public static HashedFile fromString(String string) {
    var split = string.split(SEPARATOR);
    return new HashedFile(split[0], split[1]);
  }
}