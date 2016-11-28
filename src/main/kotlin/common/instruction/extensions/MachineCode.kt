package common.instruction.extensions

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
fun Long.opcode() = this.bits(31, 26)
fun Long.rs() = this.bits(25, 21)
fun Long.rt() = this.bits(20, 16)
fun Long.rd() = this.bits(15, 11)
fun Long.shamt() = this.bits(10, 6)

// This is the same as: return 0b111111 & this;
fun Long.funct() = 63 and this.toInt()

fun Long.offset() = this.bits(15, 0)
fun Long.target() = this.bits(25, 0)
fun Long.hint() = this.bits(20, 16)

// Works as follows:
//
// val testNumber = 0b00000001010010110100100000100000
// We'll select these 4 bits --^--^
// val expected =            0b1011
//
// testNumber.bits(19, 16) == expected
fun Long.bits(upperIndex: Int, lowerIndex: Int): Int {
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
  val shifted = this shr lowerIndex

  // Kotlin obscures the meaning a bit. Read
  // final String unwanted = new String(new char[32 - len]).replace('\0', '0');
  val unwanted = String(CharArray(32 - len)).replace('\u0000', '0')
  val requested = String(CharArray(len)).replace('\u0000', '1')
  val mask = unwanted + requested

  return (shifted and Integer.valueOf(mask, 2).toLong()).toInt()
}