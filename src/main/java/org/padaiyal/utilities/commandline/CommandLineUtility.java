package org.padaiyal.utilities.commandline;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.padaiyal.utilities.I18nUtility;
import org.padaiyal.utilities.PropertyUtility;
import org.padaiyal.utilities.commandline.exceptions.CommandLineNotFoundException;

/**
 * Used to execute commands in a specified or auto-detected terminal/shell.
 */
public final class CommandLineUtility {

  /**
   * Used to log information and errors for this class.
   */
  private static final Logger logger = LogManager.getLogger(CommandLineUtility.class);
  /**
   * Retrieves and stores the operating system of the machine in which this code is executing.
   */
  private static final OperatingSystem operatingSystem = OperatingSystem.getOperatingSystem();
  /**
   * Used to store the path to all identified command line executables in this machine.
   */
  private static final HashMap<TypeOfCommandLine, Path> commandLines = new HashMap<>();
  /**
   * Used to prevent simultaneous initialization of property file, commandLines etc.
   */
  private static final ReentrantLock dependantValuesInitializationLock = new ReentrantLock();

  static {
    I18nUtility.addResourceBundle(
        CommandLineUtility.class,
        "CommandLineUtility_i18n",
        Locale.US
    );
  }

  /**
   * Empty private constructor as this utility class is not meant to be used as an instance.
   */
  private CommandLineUtility() {
  }

  /**
   * Initialize static variables needed for this utility.
   *
   * @throws IOException If there is an issue adding the property file.
   */
  private static void initializeDependantValues() throws IOException {
    if (!dependantValuesInitializationLock.isLocked() && dependantValuesInitializationLock
        .tryLock()) {
      try {
        PropertyUtility.addPropertyFile(
            CommandLineUtility.class,
            "CommandLineUtility.properties"
        );

        for (TypeOfCommandLine typeOfCommandLine : TypeOfCommandLine.values()) {
          try {
            String commandLinePath = typeOfCommandLine.getCommandLineLocation(operatingSystem);
            commandLines.put(
                typeOfCommandLine,
                Paths.get(commandLinePath)
            );
          } catch (
          CommandLineNotFoundException
              | InterruptedException
              | TimeoutException
              | IOException e
          ) {
            logger.warn(e);
          }
        }
      } finally {
        dependantValuesInitializationLock.unlock();
      }
    }
  }

  /**
   * Split the given command and arguments into an array using the space as a delimiter.
   *
   * @param command The command string to parse and split.
   * @return The array containing the split command and arguments.
   */
  private static String[] splitArgs(String command) {
    Objects.requireNonNull(command);

    System.out.println("Command before splitting: " + command);

    String trimmedCmd = command.trim();
    List<Character> delimiters = Arrays.asList('"', '\'');
    List<String> args = new ArrayList<>();
    char tempDelimiter = ' ';
    int startIndex = -1;
    int endIndex = -1;
    boolean flag = false;

    if (!trimmedCmd.contains(" ")) {
      return new String[]{trimmedCmd};
    }

    for (int i = 0; i < trimmedCmd.length(); i++) {
      if (
          tempDelimiter == ' '
              && ((i == 0) || (trimmedCmd.charAt(i - 1) == ' '))
              && delimiters.contains(trimmedCmd.charAt(i))
      ) {
        tempDelimiter = trimmedCmd.charAt(i);
        startIndex = i + 1;
        flag = true;
      } else if (
          flag && tempDelimiter != ' '
              && (trimmedCmd.charAt(i) == tempDelimiter)
              && ((i == trimmedCmd.length() - 1) || (trimmedCmd.charAt(i + 1) == ' '))
      ) {
        tempDelimiter = ' ';
        endIndex = i;
        flag = false;
      } else if (i == 0 && startIndex == -1) { // Space is the delimiter
        startIndex = i;
      } else if (i == trimmedCmd.length() - 1 && startIndex != -1) {
        endIndex = i + 1;
      } else if (trimmedCmd.charAt(i) == ' ' && startIndex == -1) {
        startIndex = i;
      } else if (!flag && trimmedCmd.charAt(i + 1) == ' ' && endIndex == -1) {
        endIndex = i + 1;
      }

      if (startIndex >= 0 && endIndex >= 0) {
        args.add(trimmedCmd.substring(startIndex, endIndex).trim());
        startIndex = -1;
        endIndex = -1;
      }
    }
    System.out.println("Command after splitting: " + Arrays.toString(args.toArray(new String[0])));
    return args.toArray(new String[0]);
  }

  /**
   * Executes the specified command.
   *
   * @param command         Command string to execute.
   * @param timeOutDuration Time out for the command execution.
   * @return The response of the command.
   * @throws IOException          Thrown by ProcessBuilder::waitFor.
   * @throws InterruptedException Thrown if the execution of the command is interrupted.
   * @throws TimeoutException     Thrown f the command execution exceeds specified timeout.
   */
  public static synchronized Response executeCommand(
      String command,
      Duration timeOutDuration
  ) throws IOException, InterruptedException, TimeoutException {
    Objects.requireNonNull(command,
        I18nUtility.getFormattedString(
            "CommandLineUtility.input.validation.nonNull",
            "Command"
        )
    );
    Objects.requireNonNull(timeOutDuration,
        I18nUtility.getFormattedString(
            "CommandLineUtility.input.validation.nonNull",
            "Duration"
        )
    );
    logger.info(
        I18nUtility.getFormattedString(
            "CommandLineUtility.input.validation.nonNull",
            command
        )
    );

    initializeDependantValues();

    logger.info(
        I18nUtility.getString("CommandLineUtility.executing"),
        command
    );

    ProcessBuilder processBuilder = new ProcessBuilder(splitArgs(command));
    final Instant executionStartTimestamp = Instant.now();
    Process process = processBuilder.start();
    boolean timedOut = !process.waitFor(timeOutDuration.getSeconds(), TimeUnit.SECONDS);
    if (timedOut) {
      Duration executionDuration = Duration.between(executionStartTimestamp, Instant.now());
      throw new TimeoutException(
          I18nUtility.getFormattedString("CommandLineUtility.exception.TimeoutException",
              command,
              executionDuration.getSeconds(),
              timeOutDuration.getSeconds()
          )
      );
    }
    HashMap<TypeOfOutput, String> output = new HashMap<>();
    final int returnCode = process.waitFor();
    final Instant executionEndTimestamp = Instant.now();
    output.put(TypeOfOutput.STDOUT,
        StreamUtility.convertInputStreamToString(process.getInputStream()));
    output.put(TypeOfOutput.STDERR,
        StreamUtility.convertInputStreamToString(process.getErrorStream()));
    process.destroy();
    return new Response(returnCode, output, executionStartTimestamp, executionEndTimestamp);
  }

  /**
   * Executes the specified command.
   *
   * @param command Command string to execute.
   * @return The response of the command.
   * @throws IOException          Thrown by ProcessBuilder::waitFor.
   * @throws InterruptedException Thrown if the execution of the command is interrupted.
   * @throws TimeoutException     Thrown f the command execution exceeds specified timeout.
   */
  public static Response executeCommand(String command) throws IOException, InterruptedException,
      TimeoutException {
    initializeDependantValues();
    return executeCommand(command, Duration
        .ofSeconds(PropertyUtility.getTypedProperty(Long.class, "CommandLineUtility.timeout")));
  }

  /**
   * Executes the specified command.
   *
   * @param command           Command string to execute.
   * @param typeOfCommandLine The type of command line in which the command needs to be executed
   *                          in.
   * @param timeOutDuration   Time out for the command execution.
   * @return The response of the command.
   * @throws IOException          Thrown by ProcessBuilder::waitFor.
   * @throws InterruptedException Thrown if the execution of the command is interrupted.
   * @throws TimeoutException     Thrown f the command execution exceeds specified timeout.
   */
  public static Response executeCommand(
      Command command,
      TypeOfCommandLine typeOfCommandLine,
      Duration timeOutDuration
  ) throws IOException, InterruptedException, CommandLineNotFoundException, TimeoutException {
    Objects.requireNonNull(
        command,
        I18nUtility.getFormattedString(
            "CommandLineUtility.input.validation.nonNull",
            "Command"
        )
    );
    Objects.requireNonNull(
        typeOfCommandLine,
        I18nUtility.getFormattedString(
            "CommandLineUtility.input.validation.nonNull",
            "TypeOfCommandLine"
        )
    );
    Objects.requireNonNull(
        timeOutDuration,
        I18nUtility.getFormattedString(
            "CommandLineUtility.input.validation.nonNull",
            "Duration"
        )
    );
    if (commandLines.containsKey(typeOfCommandLine)) {
      return executeCommand(
          String.format(
              TypeOfCommandLine.getCommandTemplate(typeOfCommandLine),
              commandLines.get(typeOfCommandLine)
                  .toAbsolutePath()
                  .toString(),
              command.getCommand(typeOfCommandLine)
          ),
          timeOutDuration
      );
    } else {
      throw new CommandLineNotFoundException(typeOfCommandLine);
    }
  }

  /**
   * Executes the specified command.
   *
   * @param command           Command to execute.
   * @param typeOfCommandLine The type of command line in which the command needs to be executed
   *                          in.
   * @return The response of the command.
   * @throws IOException          Thrown by ProcessBuilder::waitFor.
   * @throws InterruptedException Thrown if the execution of the command is interrupted.
   * @throws TimeoutException     Thrown f the command execution exceeds specified timeout.
   */
  public static Response executeCommand(
      Command command,
      TypeOfCommandLine typeOfCommandLine
  ) throws IOException, InterruptedException, TimeoutException, CommandLineNotFoundException {
    return executeCommand(command, typeOfCommandLine, Duration
        .ofSeconds(Long.parseLong(PropertyUtility.getProperty("CommandLineUtility.timeout"))));
  }

  /**
   * Executes the specified command.
   *
   * @param command         Command to execute.
   * @param timeOutDuration Time out for the command execution.
   * @return The response of the command.
   * @throws IOException          Thrown by ProcessBuilder::waitFor.
   * @throws InterruptedException Thrown if the execution of the command is interrupted.
   * @throws TimeoutException     Thrown if the command execution exceeds specified timeout.
   */
  public static Response executeCommand(Command command, Duration timeOutDuration)
      throws IOException, InterruptedException, TimeoutException, CommandLineNotFoundException {
    TypeOfCommandLine[] supportedTypeOfCommandLines
        = operatingSystem.getSupportedTypeOfCommandLines();
    Response response = null;
    int index = 0;
    boolean validResponse = false;
    while (!validResponse) {
      response = executeCommand(command, supportedTypeOfCommandLines[index], timeOutDuration);
      index++;
      validResponse = true;
    }

    return response;
  }

  /**
   * Executes the specified command.
   *
   * @param command Command to execute.
   * @return The response of the command.
   * @throws IOException          Thrown by ProcessBuilder::waitFor.
   * @throws InterruptedException Thrown if the execution of the command is interrupted.
   * @throws TimeoutException     Thrown f the command execution exceeds specified timeout.
   */
  public static Response executeCommand(Command command)
      throws IOException,
      InterruptedException,
      TimeoutException, CommandLineNotFoundException {
    return executeCommand(command, Duration
        .ofSeconds(Long.parseLong(PropertyUtility.getProperty("CommandLineUtility.timeout"))));
  }

  /**
   * Enum to represent the type of command output.
   */
  public enum TypeOfOutput {
    STDOUT,
    STDERR
  }

  /**
   * Enum to represent the type of command line to use to execute commands.
   */
  public enum TypeOfCommandLine {
    @SuppressWarnings("SpellCheckingInspection")
    POWERSHELL,
    CMD,
    BASH,
    ZSH;

    /**
     * Returns the template to execute a command using a specified command line.
     *
     * @param typeOfCommandLine Type of command line for which a template is to be retrieved and
     *                          returned.
     * @return The template to execute a command in the specified command line.
     */
    public static String getCommandTemplate(TypeOfCommandLine typeOfCommandLine) {
      return PropertyUtility
          .getProperty("CommandLineUtility.template." + typeOfCommandLine.toString());
    }

    /**
     * Given the operating system of this machine, this method gets the path of the command line
     * executable denoted by this enum object.
     *
     * @param operatingSystem Operating system of the machine in which this code is executed.
     * @return The path to the command line represented by this enum object.
     * @throws InterruptedException         If the process of identifying the commandline executable
     *                                      is interrupted.
     * @throws TimeoutException             If the process of identifying the commandline executable
     *                                      times out.
     * @throws IOException                  If there is an issue with identifying the commandline
     *                                      executable.
     * @throws CommandLineNotFoundException If the command line executable cannot be found.
     */
    public String getCommandLineLocation(OperatingSystem operatingSystem)
        throws InterruptedException, TimeoutException, IOException, CommandLineNotFoundException {
      String command = String.format(
          operatingSystem.getCommandStringForDetectingCommandLines(),
          this.toString().toLowerCase()
      );
      Response response;

      response = executeCommand(command);
      String commandLinePath = null;
      if (response.getReturnCode() == 0) {
        // If multiple command line paths are found, choose the first one.
        commandLinePath = Arrays.stream(
            response.getOutput(TypeOfOutput.STDOUT)
                .trim()
                .split("[\n\r]")
        )
            .findFirst()
            .orElse(null);
      }
      if (commandLinePath != null) {
        commandLinePath = commandLinePath.trim();
      } else {
        throw new CommandLineNotFoundException(this);
      }
      return commandLinePath;
    }
  }

}
