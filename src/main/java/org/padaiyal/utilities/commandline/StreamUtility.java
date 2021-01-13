package org.padaiyal.utilities.commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility to manipulate streams.
 */
public final class StreamUtility {

  /**
   * Empty private constructor as this utility class is not meant to be used as an instance.
   */
  private StreamUtility() {

  }

  /**
   * Extracts the information from a given InputStream as a string.
   *
   * @param inputStream The InputStream to parse.
   * @return The string extracted from the provided InputStream.
   */
  public static String convertInputStreamToString(InputStream inputStream) throws IOException {
    String result;
    try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
      result = bufferedReader.lines()
          .reduce("", (x, y) -> x + "\n" + y)
          .trim();
    }
    return result;
  }

}

