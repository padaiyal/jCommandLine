package org.padaiyal.utilities.commandline;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.padaiyal.utilities.commandline.CommandLineUtility.TypeOfCommandLine;
import org.padaiyal.utilities.commandline.exceptions.CommandLineNotFoundException;

/**
 * Tests for CommandLineUtility.
 */
class CommandLineUtilityTest {

  /**
   * Normal test command.
   */
  private static Command command;
  /**
   * Non terminating test command.
   */
  private static Command nonTerminatingCommand;
  /**
   * Test command timeout duration.
   */
  private static Duration timeoutDuration;
  /**
   * Operating system of test machine.
   */
  private static OperatingSystem operatingSystem;
  /**
   * Type of command line to test.
   */
  private static TypeOfCommandLine typeOfCommandLine;
  /**
   * Logger object.
   */
  private final Logger logger = LogManager.getLogger(CommandLineUtilityTest.class);

  /**
   * Prepares the static variables used for the tests.
   */
  @BeforeAll
  static void prepare() {
    command = new Command();
    command.setCommand(TypeOfCommandLine.BASH, "ls");
    command.setCommand(TypeOfCommandLine.CMD, "dir");
    command.setCommand(TypeOfCommandLine.POWERSHELL, "dir");
    command.setCommand(TypeOfCommandLine.ZSH, "ls");

    nonTerminatingCommand = new Command();
    nonTerminatingCommand.setCommand(TypeOfCommandLine.BASH, "cat");
    nonTerminatingCommand
        .setCommand(TypeOfCommandLine.CMD, "ping 127.0.0.1 -n 4294967295");
    nonTerminatingCommand
        .setCommand(TypeOfCommandLine.POWERSHELL, "ping 127.0.0.1 -n 4294967295");
    nonTerminatingCommand.setCommand(TypeOfCommandLine.ZSH, "cat");

    timeoutDuration = Duration.ofSeconds(5);

    operatingSystem = OperatingSystem.getOperatingSystem();

    typeOfCommandLine = switch (operatingSystem) {
      case WINDOWS -> TypeOfCommandLine.CMD;
      case LINUX -> TypeOfCommandLine.BASH;
      case MAC_OS_X -> TypeOfCommandLine.ZSH;
      default -> null;
    };
  }

  @Test
  void testExecuteCommandString() throws IOException, InterruptedException,
      TimeoutException, CommandLineNotFoundException {
    String commandString = command.getCommand(typeOfCommandLine);
    Response response = CommandLineUtility.executeCommand(commandString);
    Assertions.assertEquals(0, response.getReturnCode());
    Assertions.assertNotNull(response.getExecutionDuration());
    Assertions.assertNotNull(response.getExecutionStartTimestamp());
    Assertions.assertNotNull(response.getExecutionEndTimestamp());
  }

  @Test
  void testExecuteCommandStringWithTimeout() throws IOException, InterruptedException,
      TimeoutException, CommandLineNotFoundException {
    String commandString = command.getCommand(typeOfCommandLine);
    Response response = CommandLineUtility.executeCommand(commandString, timeoutDuration);
    Assertions.assertEquals(0, response.getReturnCode());
  }

  @Test
  void testExecuteNonTerminatingCommandStringWithTimeout()
      throws IOException, InterruptedException, CommandLineNotFoundException {
    boolean timeoutExceptionThrown = false;
    String nonTerminatingCommandString = nonTerminatingCommand.getCommand(typeOfCommandLine);
    try {
      CommandLineUtility.executeCommand(nonTerminatingCommandString, timeoutDuration);
    } catch (TimeoutException e) {
      timeoutExceptionThrown = true;
    }
    Assertions.assertTrue(timeoutExceptionThrown);
  }

  @Test
  void testExecuteCommand()
      throws IOException, InterruptedException, TimeoutException, CommandLineNotFoundException {
    Response response = CommandLineUtility.executeCommand(command);
    Assertions.assertEquals(0, response.getReturnCode());
  }

  @Test
  void testExecuteCommandWithTimeout() throws IOException, InterruptedException,
      TimeoutException, CommandLineNotFoundException {
    Response response = CommandLineUtility.executeCommand(command, timeoutDuration);
    Assertions.assertEquals(0, response.getReturnCode());
  }

  @Test
  void testExecuteNonTerminatingCommandWithTimeout() throws IOException, InterruptedException {
    boolean timeoutExceptionThrown = false;
    try {
      CommandLineUtility.executeCommand(nonTerminatingCommand, timeoutDuration);
    } catch (TimeoutException | CommandLineNotFoundException e) {
      timeoutExceptionThrown = true;
    }
    Assertions.assertTrue(timeoutExceptionThrown);
  }

  @Test
  void testExecuteCommandWithTypeOfShell() throws IOException, InterruptedException,
      TimeoutException, CommandLineNotFoundException {
    Response response = CommandLineUtility.executeCommand(command, typeOfCommandLine);
    Assertions.assertEquals(0, response.getReturnCode());
  }

  @Test
  void testExecuteCommandWithTypeOfShellAndTimeout()
      throws IOException, InterruptedException, TimeoutException,
      CommandLineNotFoundException {
    Response response = CommandLineUtility.executeCommand(
        command,
        typeOfCommandLine,
        timeoutDuration
    );
    Assertions.assertEquals(0, response.getReturnCode());
  }

  @Test
  void testExecuteNonTerminatingCommandWithTypeOfShellAndTimeout()
      throws IOException, InterruptedException, CommandLineNotFoundException {
    boolean timeoutExceptionThrown = false;
    try {
      CommandLineUtility.executeCommand(nonTerminatingCommand, typeOfCommandLine, timeoutDuration);
    } catch (TimeoutException e) {
      timeoutExceptionThrown = true;
    }
    Assertions.assertTrue(timeoutExceptionThrown);
  }

  @Test
  void testExecuteNullCommandString() throws IOException, InterruptedException, TimeoutException {
    boolean npeThrown = false;
    try {
      CommandLineUtility.executeCommand((String) null);
    } catch (NullPointerException e) {
      npeThrown = true;
    }
    Assertions.assertTrue(npeThrown);
  }

  @Test
  void testExecuteNullCommandStringWithNullTimeout()
      throws IOException, InterruptedException, TimeoutException {
    boolean npeThrown = false;
    try {
      //noinspection ConstantConditions
      CommandLineUtility.executeCommand((String) null, null);
    } catch (NullPointerException e) {
      npeThrown = true;
    }
    Assertions.assertTrue(npeThrown);
  }

  @Test
  void testExecuteNullCommand() throws IOException, InterruptedException, TimeoutException {
    boolean npeThrown = false;
    try {
      CommandLineUtility.executeCommand((Command) null);
    } catch (NullPointerException | CommandLineNotFoundException e) {
      npeThrown = true;
    }
    Assertions.assertTrue(npeThrown);
  }

  @Test
  void testExecuteNullCommandWithNullTimeout() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> CommandLineUtility.executeCommand((Command) null, (Duration) null)
    );
  }

  @Test
  void testExecuteNullCommandWithNullTypeOfShell() throws IOException, InterruptedException,
      TimeoutException, CommandLineNotFoundException {
    boolean npeThrown = false;
    try {
      CommandLineUtility.executeCommand(null, (TypeOfCommandLine) null);
    } catch (NullPointerException e) {
      npeThrown = true;
    }
    Assertions.assertTrue(npeThrown);
  }

  @Test
  void testExecuteNullCommandWithNullTypeOfShellAndNullTimeout() throws IOException,
      InterruptedException, TimeoutException, CommandLineNotFoundException {
    boolean npeThrown = false;
    try {
      CommandLineUtility.executeCommand(null, null, null);
    } catch (NullPointerException e) {
      npeThrown = true;
    }
    Assertions.assertTrue(npeThrown);
  }

  @Test
  void testExecuteCommandForUnspecifiedShell() {
    Assertions.assertThrows(CommandLineNotFoundException.class, () -> {
      Command command = new Command();
      CommandLineUtility.executeCommand(command, TypeOfCommandLine.BASH);
    });
  }

  private Set<TypeOfCommandLine> getUnsupportedTypeOfCommandLines(OperatingSystem operatingSystem) {
    Set<TypeOfCommandLine> availableTypeOfCommandLines = new HashSet<>(
        Arrays.asList(
            TypeOfCommandLine.values()
        )
    );
    HashSet<TypeOfCommandLine> supportedTypeOfCommandLines = new HashSet<>(
        Arrays.asList(
            operatingSystem.getSupportedTypeOfCommandLines()
        )
    );
    HashSet<TypeOfCommandLine> unsupportedTypeOfCommandLines = new HashSet<>(
        availableTypeOfCommandLines);
    unsupportedTypeOfCommandLines.removeAll(supportedTypeOfCommandLines);

    return unsupportedTypeOfCommandLines;
  }

  @Test
  void testExecuteCommandForInvalidCommandLine() {
    Set<TypeOfCommandLine> unsupportedTypeOfCommandLines = getUnsupportedTypeOfCommandLines(
        operatingSystem);

    if (unsupportedTypeOfCommandLines.size() == 0) {
      logger.info(
          "No unsupported command lines found for {} operating system.",
          operatingSystem
      );
    }

    logger.info(
        "Unsupported command lines for {} operating system are: {}",
        operatingSystem,
        unsupportedTypeOfCommandLines
    );

    for (TypeOfCommandLine unsupportedTypeOfCommandLine : unsupportedTypeOfCommandLines) {
      Assertions.assertThrows(
          CommandLineNotFoundException.class,
          () -> CommandLineUtility.executeCommand(
              command,
              unsupportedTypeOfCommandLine,
              Duration.ofSeconds(5)
          )
      );
    }
  }

}
