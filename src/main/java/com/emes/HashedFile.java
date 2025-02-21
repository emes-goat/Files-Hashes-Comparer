package com.emes;

public record HashedFile(
    String path,
    String hash
) {

  @Override
  public String toString() {
    return path + "," + hash;
  }
}