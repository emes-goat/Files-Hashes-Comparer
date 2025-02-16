package com.emes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class HashesCalculatorTest {

  private static final Path directory = Paths.get("hellokitty");
  private static final Path fileAName = Paths.get("afile");
  private static final Path fileBName = Paths.get("bfile");

  @Test
  public void happyPath() throws IOException {
    cleanup();

    Files.createDirectory(directory);
    var fileAContent = "Welcome to the jungle";
    var fileBContent = "Soft kitty warm kitty";
    Files.writeString(directory.resolve(fileAName), fileAContent);
    Files.writeString(directory.resolve(fileBName), fileBContent);

    var mainRunner = new HashesCalculator();
    mainRunner.calculate(directory);

    //Doft instead of Soft
    var fileBContentChanged = "Doft kitty warm kitty";
    Files.writeString(directory.resolve(fileBName), fileBContentChanged);
    mainRunner.calculate(directory);

    var hashesComparer = new HashesComparer();
    hashesComparer.compare(directory);

    assertEquals(1, 1);
  }

  @AfterAll
  public static void cleanup() throws IOException {
    if (Files.exists(directory)) {
      Files.walkFileTree(directory,
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult postVisitDirectory(
                Path dir, IOException exc) throws IOException {
              Files.delete(dir);
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(
                Path file, BasicFileAttributes attrs)
                throws IOException {
              Files.delete(file);
              return FileVisitResult.CONTINUE;
            }
          });
    }
  }
}