package common.instruction

import common.instruction.decomposedrepresentation.DecomposedRepresentation

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
fun Long.opcode() = this shr 26
fun Long.rs() = this.toInt().rs()
fun Long.rt() = this.toInt().rt()
fun Long.rd() = this.toInt().rd()
fun Long.shamt() = this.toInt().shamt()
fun Long.funct() = this.toInt().funct()
fun Long.offset() = this.toInt().offset()
fun Long.target() = this.toInt().target()
fun Long.hint() = this.toInt().hint()

/* Convenience functions */
fun Int.opcode() = this shr 26
fun Int.rs() = DecomposedRepresentation.bits(25, 21, this)
fun Int.rt() = DecomposedRepresentation.bits(20, 16, this)
fun Int.rd() = DecomposedRepresentation.bits(15, 11, this)
fun Int.shamt() = DecomposedRepresentation.bits(10, 6, this)
fun Int.offset() = DecomposedRepresentation.bits(15, 0, this)
fun Int.target() = DecomposedRepresentation.bits(25, 0, this)
fun Int.hint() = DecomposedRepresentation.bits(20, 16, this)

// This is the same as: return 0b111111 & this;
fun Int.funct() = 63 and this