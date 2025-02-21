package com.emes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FileTreeHashTest {

  private static final Path directory = Paths.get("./hellokitty");
  private static final Path fileAName = Paths.get("afile");
  private static final Path fileBName = Paths.get("bfile");

  @Test
  public void happyPath() throws IOException, InterruptedException {
    var hashesCalculator = new FileTreeHash();
    Files.createDirectory(directory);

    Files.writeString(directory.resolve(fileAName), "Something");
    var actual = hashesCalculator.calculateAndCompare(directory);
    assertEquals(0, actual.size());
    var previousHash = hashesCalculator.calculateForFile(directory.resolve(fileAName));
    Thread.sleep(50);

    var fileAContent = "Welcome to the jungle";
    var fileBContent = "Soft kitty warm kitty";
    Files.writeString(directory.resolve(fileAName), fileAContent);
    Files.writeString(directory.resolve(fileBName), fileBContent);
    actual = hashesCalculator.calculateAndCompare(directory);
    var expected = List.of(
        new ChangedHash(fileAName, previousHash,
            hashesCalculator.calculateForFile(directory.resolve(fileAName)))
    );
    assertIterableEquals(expected, actual);
    previousHash = hashesCalculator.calculateForFile(directory.resolve(fileBName));
    Thread.sleep(50);

    //Doft instead of Soft
    var fileBContentChanged = "Doft kitty warm kitty";
    Files.writeString(directory.resolve(fileBName), fileBContentChanged);
    actual = hashesCalculator.calculateAndCompare(directory);
    expected = List.of(
        new ChangedHash(fileBName, previousHash,
            hashesCalculator.calculateForFile(directory.resolve(fileBName)))
    );
    assertIterableEquals(expected, actual);

    Thread.sleep(50);

    actual = hashesCalculator.calculateAndCompare(directory);
    assertEquals(0, actual.size());
  }

  @BeforeAll
  public static void prepare() throws IOException {
    cleanup();
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