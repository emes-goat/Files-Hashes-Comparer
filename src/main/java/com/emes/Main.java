package com.emes;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

  private final static HashesCalculator HASHES_CALCULATOR = new HashesCalculator();

  public static void main(String[] args) {
    var options = new Options();

    Option directory = Option.builder("directory")
        .argName("directory")
        .hasArg()
        .desc("Root directory for the files to have their hashes calculated")
        .required()
        .build();

    options.addOption(directory);

    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine line = parser.parse(options, args);
      HASHES_CALCULATOR.calculate(tryConvertDirectory(line.getOptionValue(directory)));
    } catch (ParseException exp) {
      System.err.println("Parsing failed. Reason: " + exp.getMessage());
    }
  }

  private static Path tryConvertDirectory(String directory) {
    try {
      return Paths.get(directory);
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException(String.format("%s isn't a file", directory));
    }
  }
}