package org.padaiyal.utilities.commandline;

import java.util.HashMap;
import org.padaiyal.utilities.commandline.CommandLineUtility.TypeOfCommandLine;
import org.padaiyal.utilities.commandline.exceptions.CommandLineNotFoundException;

/**
 * Represents the command that needs to be executed along with the equivalent commands for
 * different operating systems.
 */
public class Command {

  /**
   * Stores all equivalent commands with respect to support command lines.
   */
  private final HashMap<TypeOfCommandLine, String> commands;

  Command() {
    commands = new HashMap<>();
  }

  /**
   * Set the command to execute for the specified type of shell.
   *
   * @param shellType Type of shell.
   * @param command   Command to execute in the specified shell type.
   */
  public void setCommand(TypeOfCommandLine shellType, String command) {
    commands.put(shellType, command);
  }

  /**
   * Get the command to execute for the specified type of shell.
   *
   * @param shellType Type of shell.
   * @return Command to execute in the specified shell type.
   * @throws CommandLineNotFoundException Thrown if no command is set for the specified shell
   *                                      type.
   */
  public String getCommand(TypeOfCommandLine shellType) throws CommandLineNotFoundException {
    if (commands.containsKey(shellType)) {
      return commands.get(shellType);
    } else {
      throw new CommandLineNotFoundException(shellType);
    }
  }
}
