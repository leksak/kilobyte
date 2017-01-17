package kilobyte.common.instruction.decomposedrepresentation;

import lombok.Value;

import java.util.Arrays;
import java.util.StringJoiner;

@Value
public class DecomposedRepresentation {
  int[] decomposition;
  long numericalRepresentation;

  private DecomposedRepresentation(long numericalRepresentation, int[] decomposition) {
    this.numericalRepresentation = numericalRepresentation;
    this.decomposition = new int[decomposition.length];
    for (int i = 0; i < decomposition.length; i++) {
      this.decomposition[i] = (int) Integer.toUnsignedLong(decomposition[i]) & 0xffff;
    }
  }

  /**
   * Decomposes the given number into chunks that have the specified lengths.
   * The sum of the supplied lengths must be equal to 32.
   * <p>
   * For an example: The number 0x71014802 when decomposed into chunks
   * of lengths (6,5,5,5,5,6) yields the representation [0x1c, 8, 1, 9, 0, 2]
   * <p>
   * To get the aforementioned decomposition call the method like so:
   * <pre>{@code fromNumber(0x71014802, 6, 5, 5, 5, 5, 6)}</pre>
   *
   * @param number  the numerical representation of the number to decompose.
   * @param lengths the length of each chunk if {@code number} was in base 2.
   * @return the decomposed representation of {@code number} into chunks
   * where each chunk matches (in order) the supplied lengths.
   */
  public static DecomposedRepresentation fromNumber(long number, int... lengths) {
    if (Arrays.stream(lengths).sum() != 32) {
      String err = "Expected the sum of \"lengths\" to be 32. Got: ";
      err += Arrays.stream(lengths).sum();
      throw new IllegalArgumentException(err);
    }

    int[] decomposition = new int[lengths.length];
    int start = 0;
    for (int i = 0; i < lengths.length; i++) {
      decomposition[i] = getNBits(number, start, lengths[i]);
      start += lengths[i];
    }

    return new DecomposedRepresentation(number, decomposition);
  }

  public static DecomposedRepresentation fromIntArray(int[] bitfields, int... lengths) {
    if (Arrays.stream(lengths).sum() != 32) {
      String err = "Expected the sum of \"lengths\" to be 32. Got: ";
      err += Arrays.stream(lengths).sum();
      throw new IllegalArgumentException(err);
    }

    int[] decomposition = new int[lengths.length];
    int next_shiftAmount = lengths[lengths.length - 1];
    int current_shiftAmount = 0;

    int composed = 0;

    for (int i = lengths.length - 1; i >= 0; i--) {
      int bitfieldVal = bitfields[i];
      decomposition[i] = bitfieldVal;

      if (bitfieldVal < 0) {
        int unsigned = (int) (Integer.toUnsignedLong(bitfieldVal) & 0xffff);
        composed = unsigned << (current_shiftAmount) | composed;
      } else {
        composed = (bitfieldVal << current_shiftAmount) | composed;
      }
      current_shiftAmount = next_shiftAmount;

      if (i > 0) {
        next_shiftAmount = next_shiftAmount + lengths[i - 1];
      }
    }
    return new DecomposedRepresentation(composed, decomposition);
  }

  /**
   * Given a 32-bit {@code number} and an index, {@code start}, specifying at what
   * bit position in the 32-bit {@code number}, to yank {@code numberOfBits}
   * bits from the supplied {@code number}. {@code start = 0} starts yanking
   * bits from the 32nd (MSB) bit. Valid ranges of {@code start} ranges from
   * 0-31.
   * <p>
   * For instance, consider the number
   * <p>
   * n = 0b1110001000000010100100000000010 (= 0x71014802)
   * <p>
   * Then, retrieving the leftmost six bits may be done by calling,
   * <p>
   * leftMostSixBits = getNBits(n, 0, 6) => leftMostSixBits = 28 = 0x1c
   *
   * @param number       the number to yank bits from.
   * @param start        the starting bit index from which to retrieve bits.
   * @param numberOfBits the amount of bits to retrieve.
   * @return a numeric representation of the retrieved bits.
   * @throws IllegalArgumentException if
   *                                  {@code start} does not satisfy {@code 0 <= start <= 31} or if
   *                                  {@code start + numberOfBits > 32}.
   */
  private static int getNBits(long number, int start, int numberOfBits) {
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
      pad = new String(new char[32 - length]).replace("\0",
            "0");
    }
    return pad + binaryString;
  }

  @Override
  public String toString() {
    return String.format("DecomposedRepresentation{%s, %d}", this.asDecimalString(), numericalRepresentation);
  }

  public long asLong() {
    return numericalRepresentation;
  }

  /**
   * Get the decomposed representation as an int array.
   *
   * For an example, the decomposition
   *
   * <pre>{@code d = fromNumber(0x71014802, 6, 5, 5, 5, 5, 6)}</pre>
   *
   * satisfies
   *
   * <pre>{@code Arrays.equals(d.toIntArray(), new int[] {0x1c, 8, 1, 9, 0, 2})}</pre>
   *
   * @return the decomposition as an int array
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

  public long toNumericalRepresentation() {
    return numericalRepresentation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DecomposedRepresentation that = (DecomposedRepresentation) o;

    if (numericalRepresentation != that.numericalRepresentation) return false;
    return Arrays.equals(decomposition, that.decomposition);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(decomposition);
    result = 31 * result + (int) (numericalRepresentation ^ (numericalRepresentation >>> 32));
    return result;
  }
}
