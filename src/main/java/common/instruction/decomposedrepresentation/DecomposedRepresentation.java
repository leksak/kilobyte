package common.instruction.decomposedrepresentation;

import java.util.Arrays;
import java.util.StringJoiner;

import static com.google.common.base.Preconditions.checkArgument;

public class DecomposedRepresentation {
  private final int[] decomposition;
  private final long numericalRepresentation;

  private DecomposedRepresentation(long numericalRepresentation, int[] decomposition) {
    this.numericalRepresentation = numericalRepresentation;
    this.decomposition = decomposition;
  }

  /**
   * Decomposes the given number into chunks that have the specified lengths.
   * The sum of the supplied lengths must be equal to 32.
   *
   * For an example: The number 0x71014802 when decomposed into chunks
   * of lengths (6,5,5,5,5,6) yields the representation [0x1c, 8, 1, 9, 0, 2]
   *
   * To get the aforementioned decomposition call the method like so:
   * <pre>{@code fromNumber(0x71014802, 6, 5, 5, 5, 5, 6)</pre>
   *
   * @param number the numerical representation of the number to decompose.
   * @param lengths the length of each chunk if {@code number} was in base 2.
   * @return the decomposed representation of {@code number} into chunks
   * where each chunk matches (in order) the supplied lengths.
   * @throws InvalidParameterException if the sum of {@code lengths} is not 32.
   */
  public static DecomposedRepresentation fromNumber(long number, int... lengths) {
    if (Arrays.stream(lengths).sum() != 32) {
      String err = "Expected the sum of \"lengths\" to be 32. Got: ";
      err += Arrays.stream(lengths).sum();
      throw new IllegalArgumentException(err);
    }
    assert(Arrays.stream(lengths).sum() == 32);

    int[] decomposition = new int[lengths.length];
    int start = 0;
    for (int i = 0; i < lengths.length; i++) {
      decomposition[i] = getNBits(number, start, lengths[i]);
      start += lengths[i];
    }

    return new DecomposedRepresentation(number, decomposition);
  }

  public long asLong() {
    return numericalRepresentation;
  }

  public static DecomposedRepresentation fromIntArray(int[] bitfields, int... lengths) {
    if (Arrays.stream(lengths).sum() != 32) {
      String err = "Expected the sum of \"lengths\" to be 32. Got: ";
      err += Arrays.stream(lengths).sum();
      throw new IllegalArgumentException(err);
    }
    assert(Arrays.stream(lengths).sum() == 32);

    int[] decomposition = new int[lengths.length];
    int next_shiftAmount = lengths[lengths.length - 1];
    int current_shiftAmount = 0;

    int composed = 0;

    for (int i = lengths.length - 1; i >= 0; i--) {
      int bitfieldVal = bitfields[i];
      decomposition[i] = bitfieldVal;

      composed = (bitfieldVal << current_shiftAmount) | composed;
      current_shiftAmount = next_shiftAmount;

      if (i > 0) {
        next_shiftAmount = next_shiftAmount + lengths[i - 1];
      }
    }

    return new DecomposedRepresentation(composed, decomposition);
  }

  /**
   * Get the decomposed representation as an int array.
   *
   * For an example, decomposing the number 0x71014802 into (6, 5, 5
   * For an example, the decomposition
   *
   * <pre>{@code d = fromNumber(0x71014802, 6, 5, 5, 5, 5, 6)}</pre>
   *
   * satisfies
   *
   * <pre>{@code Arrays.equals(d, new int[] {0x1c, 8, 1, 9, 0, 2})</pre>
   */
  public int[] toIntArray() {
    return decomposition;
  }

  /**
   * Returns a string representation where each chunk is represented in
   * its hexadecimal form.
   *
   * For an example, the decomposition
   *
   * <pre>{@code d = fromNumber(0x71014802, 6, 5, 5, 5, 5, 6)}</pre>
   *
   * is represented as the string "[0x1c 8 1 9 0 2]".
   *
   * @return the composition represented as a string where each field is
   * in hexadecimal form.
   */
  public String asHexadecimalString() {
    StringJoiner sj = new StringJoiner(" ", "[", "]");
    Arrays.stream(decomposition).forEach(e -> {
      String prefix = "";
      if (e > 9) {
        prefix = "0x";
      }

      sj.add(prefix + Integer.toHexString(e));
    });
    return sj.toString();
  }

  /**
   * Returns a string representation where each chunk is represented in
   * its decimal form.
   *
   * For an example, the decomposition
   *
   * <pre>{@code d = fromNumber(0x71014802, 6, 5, 5, 5, 5, 6)}</pre>
   *
   * is represented as the string "[28 8 1 9 0 2]".
   *
   * @return the composition represented as a string where each field is
   * in decimal form.
   */
  public String asDecimalString() {
    StringJoiner sj = new StringJoiner(" ", "[", "]");
    Arrays.stream(decomposition).forEach(e -> sj.add(Integer.toString(e)));
    return sj.toString();
  }

  public long toNumericalRepresentation() { return numericalRepresentation; }

  public static int bits(int upperIndex, int lowerIndex, int number) {
      checkArgument(upperIndex > lowerIndex,
            "upperIndex (%s) must be greater than lowerIndex (%s)",
            upperIndex, lowerIndex);

      checkArgument(upperIndex <= 31,
            "upperIndex (%s) must be less than (or equal to) 31",
            upperIndex);

      checkArgument(lowerIndex >= 0,
            "lowerIndex (%s) must be greater than or equal to zero",
            upperIndex, lowerIndex);

      // upperIndex denotes the MSB of the subsection of bits
      // requested, and lowerIndex the LSB.
      // The difference, upperIndex - lowerIndex equals the length
      // of the bit-sequence requested. By right-shifting "number"
      // by lowerIndex, we get a number where the requested bits
      // are in B[0] to B[X] where X is the length (upperIndex - lowerIndex)
      //
      // Now, using bitwise-AND between the shifted number and
      // 32 - (upperIndex - lowerIndex) amount of zeroes followed by
      // (upperIndex - lowerIndex) 1's we get a mask that only leaves
      // the requested bits intact.
      //
      // Take for instance the number:
      //
      // 0b0000 0001 0100 1011 0100 1000 0010 0000 = 0x014b4820
      //                  ^--^----------+
      // Now if we want these four bits |
      //
      // we'd call bits(19, 16, 0x014b4820), to get B[19]..B[16]
      // (inclusive, and with zero indexing).
      //
      // Shifting it leaves,
      //
      // 0b101001011
      //
      // we request _4_ bits, so we create a mask "1111", and prefix the
      // mask with leading 0's, effectively getting "000001011" (there
      // are actually more leading zeroes) and then we perform a bitwise
      // AND
      //
      // 0b101001011 & 0b000001111
      //
      // which leaves 1011, which is what we wanted.
      final int len = upperIndex - lowerIndex + 1;
      final int shifted = number >> lowerIndex;

      final String unwanted = new String(new char[32 - len]).replace('\0', '0');
      final String requested = new String(new char[len]).replace('\0', '1');
      final String mask = unwanted + requested;

      return shifted & Integer.valueOf(mask, 2);
    }

    /**
     * Given a 32-bit {@code number} and an index, {@code start}, specifying at what
     * bit position in the 32-bit {@code number}, to yank {@code numberOfBits}
     * bits from the supplied {@code number}. {@code start = 0} starts yanking
     * bits from the 32nd (MSB) bit. Valid ranges of {@code start} ranges from
     * 0-31.
     *
     * For instance, consider the number
     *
     * n = 0b1110001000000010100100000000010 (= 0x71014802)
     *
     * Then, retrieving the leftmost six bits may be done by calling,
     *
     * leftMostSixBits = getNBits(n, 0, 6) => leftMostSixBits = 28 = 0x1c
     *
     * @param number the number to yank bits from.
     * @param start the starting bit index from which to retrieve bits.
     * @param numberOfBits the amount of bits to retrieve.
     * @return a numeric representation of the retrieved bits.
     * @throws IllegalArgumentException if
     * {@code start} does not satisfy {@code 0 <= start <= 31} or if
     * {@code start + numberOfBits > 32}.
     */
    public static int getNBits(long number, int start, int numberOfBits) {
      if (start > 31 || start < 0) {
        throw new IllegalArgumentException(start > 31 ?
              "The supplied index \"start\" must satisfy start <= 31" :
              "The supplied index \\\"start\\\" must satisfy start >= 0");
      }
      if (start + numberOfBits > 32) {
        throw new IllegalArgumentException(
              "The argument pair \"start\" and \"numberOfBits\" must " +
                    "satisfy the condition (start + numberOfBits <= 32). " +
                    "Got start: " + start + " numberOfBits: " + numberOfBits);
      }

      String s = asBitPattern(number);
      String requestedBits = s.substring(start, start + numberOfBits);
      return Integer.parseInt(requestedBits, 2);
    }

    public static String asBitPattern(long number) {
      String binaryString = Long.toBinaryString(number);
      int length = binaryString.length();
      String pad = "";
      if (length <= 32) {
        pad = new String(new char[32-length]).replace("\0",
              "0");
      }
      return pad + binaryString;
    }
}
