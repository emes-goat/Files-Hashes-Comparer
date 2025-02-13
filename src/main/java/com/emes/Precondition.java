package com.emes;

public class Precondition {

  public static void require(boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException("Illegal argument");
    }
  }
}
