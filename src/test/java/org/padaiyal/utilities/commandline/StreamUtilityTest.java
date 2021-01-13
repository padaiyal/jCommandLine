package org.padaiyal.utilities.commandline;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for StreamUtility.
 */
class StreamUtilityTest {

  /**
   * Tests StreamUtility::convertInputStreamToString() with valid input.
   *
   * @throws IOException If there is an issue converting the input stream to a string.
   */
  @Test
  public void testConvertInputStreamToString() throws IOException {
    String string = "ZXCVBNM!@#$%";
    String stringFromInputStream = StreamUtility
        .convertInputStreamToString(new ByteArrayInputStream(string.getBytes()));
    Assertions.assertEquals(string, stringFromInputStream);
  }

  /**
   * Tests StreamUtility::convertInputStreamToString() with a null input.
   */
  @Test
  public void testConvertInputStreamToStringWithNullInput() {
    Assertions.assertThrows(NullPointerException.class,
        () -> StreamUtility.convertInputStreamToString(null));
  }

}
