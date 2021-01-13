package org.padaiyal.utilities.commandline;

import java.util.Arrays;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.padaiyal.utilities.I18nUtility;
import org.padaiyal.utilities.PropertyUtility;
import org.padaiyal.utilities.commandline.CommandLineUtility.TypeOfCommandLine;

/**
 * Enum sed to represent the operating system of a machine.
 */
public enum OperatingSystem {
  WINDOWS(
      "WINDOWS",
      "CommandLineUtility.windows.where",
      TypeOfCommandLine.CMD,
      TypeOfCommandLine.POWERSHELL
  ),
  LINUX(
      "LINUX",
      "CommandLineUtility.linux.which",
      TypeOfCommandLine.BASH
  ),
  @SuppressWarnings("SpellCheckingInspection")
  MAC_OS_X(
      "MAC OS X",
      "CommandLineUtility.macos.which",
      TypeOfCommandLine.ZSH
  ),
  UNKNOWN(
      "UNKNOWN",
      null
  );

  /**
   * Logger object.
   */
  private static final Logger logger = LogManager.getLogger(OperatingSystem.class);

  static {
    I18nUtility.addResourceBundle(
        OperatingSystem.class,
        "OperatingSystem_i18n",
        Locale.US
    );
  }

  /**
   * Operating system name.
   */
  private final String name;
  /**
   * Property key for command to detect command line executables.
   */
  private final String propertyKeyForDetectingCommandLines;
  /**
   * Supported command lines.
   */
  private final TypeOfCommandLine[] supportedCommandLines;

  /**
   * Abstracts an operating system.
   *
   * @param name                                Name of the operating system.
   * @param propertyKeyForDetectingCommandLines Property key to use to retrieve command to detect
   *                                            command line executables.
   * @param supportedCommandLines               Command lines supported by this operating system.
   */
  OperatingSystem(
      String name,
      String propertyKeyForDetectingCommandLines,
      TypeOfCommandLine... supportedCommandLines
  ) {
    this.name = name;
    this.propertyKeyForDetectingCommandLines = propertyKeyForDetectingCommandLines;
    this.supportedCommandLines = supportedCommandLines.clone();
  }

  /**
   * Detects the operating system of the machine in which this method is called.
   *
   * @return The detected operating system. If it's unable to detect, it will return UNKNOWN.
   */
  public static OperatingSystem getOperatingSystem() {
    String operatingSystemString = System.getProperty("os.name")
        .toUpperCase();
    logger.debug(
        I18nUtility.getFormattedString(
            "OperatingSystem.operatingSystemStringMessage"
        ),
        operatingSystemString
    );
    OperatingSystem detectedOperatingSystem = Arrays.stream(OperatingSystem.values())
        .filter(operatingSystem -> operatingSystemString.contains(operatingSystem.getName()))
        .findFirst()
        .orElse(OperatingSystem.UNKNOWN);
    logger.info(
        I18nUtility.getString(
            "OperatingSystem.operatingSystemDetectedMessage"
        ),
        detectedOperatingSystem
    );
    return detectedOperatingSystem;
  }

  /**
   * Gets the name of the operating system.
   *
   * @return The name of the operating system.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets the command lines supported by this operating system.
   *
   * @return The command lines supported by this operating system.
   */
  public TypeOfCommandLine[] getSupportedTypeOfCommandLines() {
    return this.supportedCommandLines
        .clone();
  }

  /**
   * Gets the command to detect command line executables.
   *
   * @return The command to detect command line executables.
   */
  public String getCommandStringForDetectingCommandLines() {
    return PropertyUtility.getProperty(propertyKeyForDetectingCommandLines);
  }
}
