package common.instruction

import com.google.common.base.Preconditions.checkArgument

/* Kotlin affords us the ability to extend primitive, and other,
 * classes - which let's us perform operations on them without
 * polluting our code with "StringUtil", "IntegerUtil", ... classes
 * populated with static methods. In Kotlin we can then write,
 *
 * 2.bits(4, 3)
 *
 * for an example (see the below "bits" function). But this does not
 * translate to Java so be wary.
 *
 * Read more here: goo.gl/8BVUYb
 *
 * Note: This shouldn't be a "Javadoc" comment. Kotlin uses KDoc
 * https://kotlinlang.org/docs/reference/kotlin-doc.html
 */

/* Convenience functions */
fun Long.opcode() = this.toInt().opcode()
fun Long.rs() = this.toInt().rs()
fun Long.rt() = this.toInt().rt()
fun Long.rd() = this.toInt().rd()
fun Long.shamt() = this.toInt().shamt()
fun Long.funct() = this.toInt().funct()

/* Convenience functions */
fun Int.opcode() = this shr 26
fun Int.rs() = this.bits(25, 21)
fun Int.rt() = this.bits(20, 16)
fun Int.rd() = this.bits(15, 11)
fun Int.shamt() = this.bits(10, 6)

// This is the same as: return 0b111111 & this;
fun Int.funct() = 63 and this

/**
 * Given a 32-bit `number` and an index, `start`, specifying at what
 * bit position in the 32-bit `number`, to yank `numberOfBits`
 * bits from the supplied `number`. `start = 0` starts yanking
 * bits from the 32nd (MSB) bit. Valid ranges of `start` ranges from
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
 *   `start` does not satisfy `0 <= start <= 31` or if
 *   `start + numberOfBits > 32`.
 */
fun Int.getNBits(start: Int, numberOfBits: Int): Int {
  if (start > 31 || start < 0) {
    throw IllegalArgumentException(if (start > 31)
      "The supplied index \"start\" must satisfy start <= 31"
    else
      "The supplied index \\\"start\\\" must satisfy start >= 0")
  }
  if (start + numberOfBits > 32) {
    throw IllegalArgumentException(
          "The argument pair \"start\" and \"numberOfBits\" must " +
                "satisfy the condition (start + numberOfBits <= 32). " +
                "Got start: " + start + " numberOfBits: " + numberOfBits)
  }

  val s = this.asBitPattern()
  val requestedBits = s.substring(start, start + numberOfBits)
  return Integer.parseInt(requestedBits, 2)
}

private fun Int.asBitPattern(): String {
  val binaryString = Integer.toBinaryString(this)
  val length = binaryString.length
  var pad = ""
  if (length <= 32) {
    pad = String(CharArray(32 - length)).replace("\u0000", "0")
  }
  return pad + binaryString
}

private fun Int.bits(upperIndex: Int, lowerIndex: Int): Int {
  checkArgument(upperIndex > lowerIndex,
        "upperIndex (%s) must be greater than lowerIndex (%s)",
        upperIndex, lowerIndex)

  checkArgument(upperIndex <= 31,
        "upperIndex (%s) must be less than (or equal to) 31",
        upperIndex)

  checkArgument(lowerIndex >= 0,
        "lowerIndex (%s) must be greater than or equal to zero",
        upperIndex, lowerIndex)

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
  val len = upperIndex - lowerIndex + 1

  // shr stands for shift right... Kotlin got it wrong there I think,
  // as >> is so ubiquitous.
  // https://kotlinlang.org/docs/reference/basic-types.html#operations
  val shifted = this shr lowerIndex

  // \u0000 is the null-character, instead of \0.
  val unwanted = String(CharArray(32 - len)).replace('\u0000', '0')
  val requested = String(CharArray(len)).replace('\u0000', '1')
  val mask = unwanted + requested

  return shifted and Integer.valueOf(mask, 2)
}