package com.emes;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

  private static final String OPTION_CALCULATE_NAME = "calculate";
  private static final String OPTION_COMPARE_NAME = "compare";
  private static final String OPTION_DIRECTORY_NAME = "directory";

  public static void main(String[] args) {
    var parser = new DefaultParser();
    var options = prepareCLIOptions();
    try {
      var parsedArguments = parser.parse(options, args);
      var directory = tryConvertDirectory(parsedArguments.getOptionValue(OPTION_DIRECTORY_NAME));
      var calculate = parsedArguments.hasOption(OPTION_CALCULATE_NAME);
      var compare = parsedArguments.hasOption(OPTION_COMPARE_NAME);
      var hashesCalculator = new HashesCalculator();

      if (calculate) {
        hashesCalculator.calculate(directory);
      }
      if (compare) {
        hashesCalculator.compare(directory);
      }
    } catch (ParseException exp) {
      System.err.println("Parsing failed. Reason: " + exp.getMessage());
    }
  }

  private static Options prepareCLIOptions() {
    var options = new Options();

    Option calculate = new Option(OPTION_CALCULATE_NAME, "Calculate hashes");
    Option compare = new Option(OPTION_COMPARE_NAME, "Compare hashes");

    Option directory = Option.builder(OPTION_DIRECTORY_NAME)
        .argName(OPTION_DIRECTORY_NAME)
        .hasArg()
        .desc("Root directory for the files to have their hashes calculated")
        .required()
        .build();

    options.addOption(calculate);
    options.addOption(compare);
    options.addOption(directory);
    return options;
  }

  private static Path tryConvertDirectory(String directory) {
    try {
      return Paths.get(directory);
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException(String.format("%s isn't a file", directory));
    }
  }
}