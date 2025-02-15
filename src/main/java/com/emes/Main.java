package com.emes;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

  public static void main(String[] args) {
    var options = new Options();

    Option password = Option.builder("password")
        .argName("password")
        .hasArg()
        .desc("Hashes file encryption password")
        .build();

    Option directory = Option.builder("directory")
        .argName("directory")
        .hasArg()
        .desc("Root directory for the files to have their hashes calculated")
        .build();

    options.addOption(password);
    options.addOption(directory);

    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine line = parser.parse(options, args);
      new MainRunner().run(line.getOptionValue(password), line.getOptionValue(directory));

    } catch (ParseException exp) {
      System.err.println("Parsing failed. Reason: " + exp.getMessage());
    }
  }
}