package com.emes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainRunner {

  private final Serializer serializer = new Serializer();
  private final AES aes = new AES();
  private final ChangedFiles changedFiles = new ChangedFiles();

  public void run(String[] args)
      throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
      IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException,
      InvalidKeyException {

    if (args.length != 2) {
      throw new RuntimeException("Invalid number of arguments");
    }
    var password = args[0];
    var rootDirectory = Paths.get(args[1]);
    if (Files.notExists(rootDirectory)) {
      throw new RuntimeException("Root directory doesn't exist");
    }

    var resultsFile = rootDirectory.resolve(".hashes");
    var now = Instant.now();
    System.out.println("Calculating hashes");
    List<HashedFile> currentResult = new FileTreeHasher().getHashes(now, rootDirectory);

    var previousResult = readPrevious(password, resultsFile);
    var changedFilesF = changedFiles.find(previousResult, currentResult);
    if (!changedFilesF.isEmpty()) {
      System.out.println("Hashes changed for files:");
      changedFilesF.forEach(System.out::println);
    } else {
      System.out.println("No changes to hashes");
    }

    // TODO Close the application with error when there is a changed file
    // TODO add lombok to handle long list of exceptions
    // TODO add CLI library to properly handle program arguments
    // TODO add more test coverage
    // TODO use file size for file checking
    // TODO use logger to handle colors in CLI

    saveCurrent(currentResult, password, resultsFile);
  }

  private void saveCurrent(List<HashedFile> currentResult, String password, Path resultsFile)
      throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException,
      IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException,
      InvalidKeySpecException, InvalidKeyException {

    var serialized = serializer.serialize(currentResult);
    var encrypted = aes.encrypt(password.toCharArray(), serialized);
    Files.write(resultsFile, encrypted);
  }

  private List<HashedFile> readPrevious(String password, Path resultsFile)
      throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException,
      IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException,
      InvalidKeySpecException, InvalidKeyException {

    if (Files.exists(resultsFile)) {
      System.out.println("Previous exists");
      var encrypted = Files.readAllBytes(resultsFile);
      var decrypted = aes.decrypt(password.toCharArray(), encrypted);
      return serializer.deserialize(decrypted);
    } else {
      return Collections.emptyList();
    }
  }
}