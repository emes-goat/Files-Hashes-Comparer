package com.emes;

public class Precondition {

  public static void require(boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException("Illegal argument");
    }
  }

  public static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalArgumentException(message);
    }
  }
}
