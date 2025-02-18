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
  public void happyPath() throws IOException, InterruptedException {
    cleanup();
    var hashesCalculator = new HashesCalculator();
    Files.createDirectory(directory);

    Files.writeString(directory.resolve(fileAName), "Something");
    hashesCalculator.calculate(directory);
    var result = hashesCalculator.compare(directory);
    assertEquals(0, result.size());

    Thread.sleep(1000);

    var fileAContent = "Welcome to the jungle";
    var fileBContent = "Soft kitty warm kitty";
    Files.writeString(directory.resolve(fileAName), fileAContent);
    Files.writeString(directory.resolve(fileBName), fileBContent);
    hashesCalculator.calculate(directory);
    result = hashesCalculator.compare(directory);
    assertEquals(1, result.size());
    assertEquals(fileAName, result.getFirst());

    Thread.sleep(1000);

    //Doft instead of Soft
    var fileBContentChanged = "Doft kitty warm kitty";
    Files.writeString(directory.resolve(fileBName), fileBContentChanged);
    hashesCalculator.calculate(directory);
    result = hashesCalculator.compare(directory);
    assertEquals(1, result.size());
    assertEquals(fileBName, result.getFirst());
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