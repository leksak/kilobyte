package common.instruction;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public interface MachineCodeDecoder {
  static boolean equals(String s1, String s2) {
    return decode(s1) == decode(s2);
  }

  static int decode(String s) {
    checkNotNull(s);
    checkArgument(s.length() >= 1,
          "Expected input argument to have non-zero length");

    s = s.trim(); // Remove trailing and leading spaces

    // Convert the string to lower-case, simplifies regex validating
    // the String format as well as matching on the prefix.
    s = s.toLowerCase();

    /*
     * Check that all characters match either hexadecimal, decimal
     * or binary strings.
     */
    if (!s.matches("-?([0-9]+)|(0[x][0-9a-f]+)|(0[b][0-1]+)|(0[d])?[0-9]+")) {
      throw new NumberFormatException(); // TODO: Add error message
    }

    if (s.length() <= 1) {
      // There can be no specified prefix
      return Integer.parseInt(s);
    }
    // Yank out the two leading characters
    String prefix = s.substring(0, 2);

    // Assuming that the number is not prefixed and then we default
    // to interpreting the number as being a decimal number (base 10)
    int base = 10;
    boolean encounteredPrefix = false;

    // The earlier .toLowerCase call allows us to switch on lower-case
    // string representations.
    switch (prefix) {
      case "0x":
        base = 16;
        encounteredPrefix = true;
        break;
      case "0b":
        base = 2;
        encounteredPrefix = true;
        break;
      case "0d":
        encounteredPrefix = true;
        break;
      default:
        // Our regex assures us that the two characters
        // that we have can only be numbers between 0 and 9
        // so treating the string as being in base 10 is
        // safe from here on.
        break;
    }

    if (encounteredPrefix) {
      // Remove the prefix from the string for the subsequent parseInt
      // call. The method call "substring(2)" splits the string at
      // the second character (exclusive) so we get that
      //
      // "0x014b4820" becomes "014b4820"
      //
      // Remove the prefix from the string, substring() takes the
      // tail starting from the second character.
      s = s.substring(2);
    }

    return Integer.parseInt(s, base);
  }

  static int decode(String s, int base) {
    return Integer.parseInt(String.valueOf(decode(s)), base);
  }
}
