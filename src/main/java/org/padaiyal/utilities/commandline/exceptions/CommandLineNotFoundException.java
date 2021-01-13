package org.padaiyal.utilities.commandline.exceptions;

import org.padaiyal.utilities.commandline.CommandLineUtility.TypeOfCommandLine;

/**
 * Represents an exception where a command line executable is not found.
 */
public class CommandLineNotFoundException extends Exception {

  /**
   * Thrown when a specified command line is not found.
   *
   * @param typeOfCommandLine Type of command line which is not found.
   */
  public CommandLineNotFoundException(TypeOfCommandLine typeOfCommandLine) {
    //      super(
    //          I18NUtility.getFormattedString(
    //              "exception.ShellNotFoundException",
    //              typeOfShell
    //          )
    //      );
    super("Command line not found - " + typeOfCommandLine);
  }
}
